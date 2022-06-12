# Oom Adjuster Designs

## Purpose of Oom Adjuster

The Android OS runs with limited hardware resources, i.e. CPU/RAM/Power. To strive for the better performance, Oom Ajuster is introduced to tweak the following 3 major factors:

* Process State（进程状态）
    * Wildly used by the System Server, i.e., determine if it's foreground or not, change the GC behavior, etc.
      系统服务广泛的使用，例如：确定进程是否是前台，以及更改GC行为等。
    * Defined in `ActivityManager#PROCESS_STATE_*`
* Oom Adj score（Oom Adj分值）
    * Used by the lmkd to determine which process should be expunged on memory pressure.
      lmkd用此分值来确定应在内存压力下删除哪个进程。
    * Defined in `ProcessList#*_ADJ`
* Scheduler Group
    * Used to tweak the process group, thread priorities.
    * Top process is scheduled to be running on a dedicated big core, while foreground processes take the other big cores; background processes stay with LITTLE cores instead.
    * 用于调整进程组、线程优先级。
    * Top进程专用的大内核上运行，而前台进程则占用其他大内核上；后台进程则运行在小内核。

## Process Capabilities

Besides the above 3 major factors, Android R introduced the Process Capabilities `ActivityManager#PROCESS_CAPABILITY_*`.

It's a new attribute to process record, mainly designed for supporting the "while-in-use" permission model - in additional to the traditional Android permissions,
wheather or not a process has access to a given API, will be guarded by its current process state as well.
这是进程的一个新属性，主要用于支持“使用中”权限模型 - 除了传统的Android权限之外，无论进程是否可以访问给定的 API，也将受到其当前进程状态的保护。

The OomAdjuster will compute the process capabilities during updating the oom adj.
Meanwhile, the flag `ActivityManager#BIND_INCLUDE_CAPABILITIES` enables to possiblity to "transfer" the capability from a client process to the service process it binds to.
OomAdjuster 将在更新 oom adj 的过程中计算进程能力。
同时，`ActivityManager#BIND_INCLUDE_CAPABILITIES` 标志可以将能力从客户端进程“转移”到它绑定的服务进程。

## Rationale of Oom Adjuster （Oom调节器的基本原理）

System server keeps a list of recent used app processes. Given the 4 types of entities that an Android processes could have: Activity, Service, Content Provider and Broadcast Receiver,
the System Server has to adjust the above 3 factors to give the users the best performance according to the states of the entities.
System server保留最近使用的应用程序进程列表。考虑到Android进程可能具有4种类型的实体：活动、服务、内容提供商和广播接收器，系统服务器必须根据实体的状态调整上述3个因素，以给用户提供最佳性能。

A typical case would be that: foreground app A binds into a background service B in order to serve the user,
in the case of memory pressure, the background service B should be avoided from being expunged since it would result user-perceptible interruption of service.
The Oom Adjuster is to tweak the aforementioned 3 factors for those app processes.
典型的情况是：前台应用程序A绑定到后台服务B以服务于用户，在内存压力的情况下，应避免删除后台服务B，因为它会导致用户感知到的服务中断。Oom调整器是为了调整上述3个应用程序进程的因素。

The timing of updating the Oom Adj score is vital: assume a camera process in background gets launched into foreground,
launching camera typically incurs high memory pressure, which could incur low memory kills - if the camera process isn't moved out of the background adj group, it could get killed by lmkd.
Therefore the updates have to be called pretty frequently: in case there is an activity start, service binding, etc.
更新Oom Adj分值的时机至关重要：假设后台的摄像头进程被启动到前台，启动摄像头通常会导致内存压力大，这可能导致low-memory kill，（如果摄像头进程没有移出后台Adj组）它可能会被lmkd杀死。
因此，必须非常频繁地调用更新：如果有活动启动、服务绑定等。


The update procedure basically consists of 3 parts:
(OOM_ADJ数值 的)更新过程基本上由3部分组成：
* Find out the process record to be updated
    * There are two categories of updateOomAdjLocked: one with the target process record to be updated, while the other one is to update all process record.
      updateOomAdjLocked有两类：一类是要更新的目标进程，一类是更新所有进程。
    * Besides that, while computing the Oom Aj score, the clients of service connections or content providers of the present process record,
      which forms a process dependency graph actually, will be evaluated as well.
      除此之外，在计算 Oom Aj 分数时，当前进程的服务连接的客户端或内容提供者，它实际上形成了一个过程依赖图，也将被评估。
    * Starting from Android R, when updating for a specific process record, an optimization is made that,
      only the reachable process records starting from this process record in the process dependency graph, will be re-evaluated.
      从 Android R 开始，针对特定进程进行更新时进行了优化，只有从流程依赖图中的该流程记录开始的可达流程记录将被重新评估。
    * The `cached` Oom Adj scores are grouped in `bucket`, which is used in the isolated processes:
      they could be correlated - assume one isolated Chrome process is at Oom Adj score 920 and another one is 980;
      the later one could get expunged much earlier than the former one, which doesn't make sense; grouping them would be a big relief for this case.
      `cached` Oom Adj 分数被分组在 `bucket`（各水桶） 中，用于隔离进程： 它们可能是相关的——假设一个孤立的 Chrome 进程的 Oom Adj 得分为 920，另一个为 980；
      后一个可能比前一个更早被删除，这是没有意义的；对于这种情况，将它们分组更为便捷。

        * Compute Oom Adj score
          计算OOM_ADJ数值
            * This procedure returns true if there is a score change, false if there is no.
              如果分数发生变化，此(计算)过程返回 true，如果没有变化，则返回 false。
            * The curAdj field in the process record is used as an intermediate value during the computation.
              进程record中的curAdj变量（field）在计算过程中作为一个中间值。
            * Initialize the Process State to `PROCESS_STATE_CACHED_EMPTY`, which is the lowest importance.
              刚初始化的进程其状态被置为`PROCESS_STATE_CACHED_EMPTY`
            * Calculate the scores based on various factors:
              计算adj数值将基于以下几种因素
                * If it's not allowed to be lower than `ProcessList#FOREGROUND_APP_ADJ`, meaning it's propbably a persistent process, there is no too much to do here.
                  如果进程不允许低于`ProcessList#FOREGROUND_APP_ADJ`，则意味着此进程应该是一个常驻进程，则此时不需要处理。
                * Exame if the process is the top app, running remote animation, running instrumentation, receiving broadcast, executing services, running on top but sleeping (screen off),
                  update the intermediate values.
                  检查进程是否是TOP app （包括：运行远程动画，运行instrumentation，接收广播，执行服务，运行在top但手机休眠中-即屏幕关闭时），则更新此curAdj值。
                * Ask Window Manager (yes, ActivityTaskManager is with WindowManager now) to tell each activity's visibility information.
                  查询ActivityTaskManager（位于WMS中）来确定每个activity当前的可见性信息。
                * Check if the process has recent tasks, check if it's hosting a foreground service, overlay UI, toast etc. Note for the foreground service, if it was in foreground status,
                  allow it to stay in higher rank in memory for a while: Assuming a camera captureing case, where the camera app is still processing the picture
                  while being switched out of foreground - keep it stay in higher rank in memory would ensure the pictures are persisted correctly.
                  检查进程是否有最近任务，检查此进程是否服务于前台服务，有覆盖的UI，或者toast弹框等等，如果进程处于前台状态，则允许此进程在内存保持更高一级状态一段时间：
                  例如当相机拍摄的情况，相机APP拍照后需要继续的处理图片，当此时此APP被切换出前台，此时正确的做法是将此进程持续保持在一个较高的级别上用来持续的处理此图片生成的过程。
                * Check if the process is the heavy weight process, whose launching/exiting would be slow and it's better to keep it in the memory.
                  Note there should be only one heavy weight process across the system.
                  检查该进程是否为重量级进程（即启动/退出速度会很慢的进程），最好将其保留在内存中。注意：整个系统中应该只有一个重型进程。
                * For sure the Home process shouldn't be expunged frequently as well.
                  当然，也不应该频繁删除HOME桌面进程。
                * The next two factors are either it was the previous process with visible UI to the user, or it's a backup agent.
                  接下来的两个因素是，它要么是用户可以看到UI的前一个进程，要么是备份代理。
                * And then it goes to the massive searches against the service connections and the content providers, each of the clients will be evaluated,
                  and the Oom Adj score could get updated according to its clients' scores. However there are a bunch of service binding flags which could impact the result:
                  然后对服务连接和ContentProviders进行大规模搜索，对每个clients端进行评估，并根据其客户的得分更新Oom Adj得分。但是，有一系列服务绑定标志可能会影响结果：

                    * （1）Below table captures the results with given various service binding states（各种服务绑定状态的结果）:
                      | Conditon #1                     | Condition #2                                               | Condition #3                                 | Condition #4                                      | Result                   |
                      |---------------------------------|------------------------------------------------------------|----------------------------------------------|---------------------------------------------------|--------------------------|
                      | `BIND_WAIVE_PRIORITY` not set   | `BIND_ALLOW_OOM_MANAGEMENT` set                            | Shown UI && Not Home                         |                                                   | Use the app's own Adj    |
                      |                                 |                                                            | Inactive for a while                         |                                                   | Use the app's own Adj    |
                      |                                 | Client has a higher importance                             | Shown UI && Not Home && client is invisible  |                                                   | Use the app's own Adj    |
                      |                                 |                                                            | `BIND_ABOVE_CLIENT` and `BIND_IMPORTANT` set | Client is not persistent                          | Try client's Adj         |
                      |                                 |                                                            |                                              | Client is persistent                              | Try persistent Adj       |
                      |                                 |                                                            | `BIND_NOT_PERCEPTIBLE` set                   | client < perceptible && app > low perceptible     | Try low perceptible Adj  |
                      |                                 |                                                            | `BIND_NOT_VISIBLE` set                       | client < perceptible && app > perceptible         | Try perceptible Adj      |
                      |                                 |                                                            | Client >= perceptible                        |                                                   | Try client's Adj         |
                      |                                 |                                                            | Adj > visible                                |                                                   | Max of client/Own Adj    |
                      |                                 |                                                            |                                              |                                                   | Use the app's own Adj    |
                      |                                 | `BIND_NOT_FOREGROUND`+`BIND_IMPORTANT_BACKGROUND` not set  | Client's sched group > app's                 | `BIND_IMPORTANT` is set                           | Use client's sched group |
                      |                                 |                                                            |                                              |                                                   | Use default sched group  |
                      |                                 |                                                            | Client's process state < top                 | `BIND_FOREGROUND_SERVICE` is set                  | ProcState = bound fg     |
                      |                                 |                                                            |                                              | `BIND_FOREGROUND_SERVICE_WHILE_AWAKE` + screen ON | ProcState = bound fg     |
                      |                                 |                                                            |                                              |                                                   | ProcState = important fg |
                      |                                 |                                                            | Client's process state = top                 |                                                   | ProcState = bound top    |
                      |                                 | `BIND_IMPORTANT_BACKGROUND` not set                        | Client's process state < transient bg        |                                                   | ProcState = transient bg |
                      |                                 | `BIND_NOT_FOREGROUND` or `BIND_IMPORTANT_BACKGROUND` set   | Client's process state < important bg        |                                                   | ProcState = important bg |
                      | `BIND_ADJUST_WITH_ACTIVITY` set | Adj > fg && App visible                                    |                                              |                                                   | Adj = foreground         |
                      |                                 |                                                            | `BIND_NOT_FOREGROUND` not set                | `BIND_IMPORTANT` is set                           | Sched = top app bound    |
                      |                                 |                                                            |                                              | `BIND_IMPORTANT` is NOT set                       | Sched = default          |

            * （2）Below table captures the results with given various content provider binding states（给定的一些ContentProvider的绑定状态）:
              | Conditon #1                     | Condition #2                                               | Condition #3                                 | Result                   |
              |---------------------------------|------------------------------------------------------------|----------------------------------------------|--------------------------|
              | Client's process state >= cached|                                                            |                                              | Client ProcState = empty |
              | Adj > Client Adj                | Not shown UI or is Home, or Client's Adj <= perceptible    | Client's Adj <= foreground Adj               | Try foreground Adj       |
              |                                 |                                                            | Client's Adj > foreground Adj                | Try client's Adj         |
              | Client's process state <= fg svc| Client's process state is top                              |                                              | ProcState = bound top    |
              |                                 | Client's process state is NOT top                          |                                              | ProcState = bound fg svc |
              | Has external dependencies       | Adj > fg app                                               |                                              | adj = fg app             |
              |                                 | Process state > important foreground                       |                                              | ProcState = important fg |
              | Still within retain time        | Adj > previous app Adj                                     |                                              | adj = previuos app adj   |
              |                                 | Process state > last activity                              |                                              | ProcState = last activity|

            * （3）Some additional tweaks after the above ones（在上述调整之后，还有一些其他调整）:
              | Conditon #1                     | Condition #2                                               | Condition #3                                 | Result                             |
              |---------------------------------|------------------------------------------------------------|----------------------------------------------|------------------------------------|
              | Process state >= cached empty   | Has client activities                                      |                                              | ProcState = cached activity client |
              |                                 | treat like activity (IME)                                  |                                              | ProcState = cached activity        |
              | Adj is service adj              | computing all process records                              | Num of new service A > 1/3 of services       | Push it to service B               |
              |                                 |                                                            | Low on RAM and app process's PSS is large    | Push it to service B               |

* Apply the scores, which consists of: write into kernel sysfs entries to update the Oom Adj scores;
  call kernel API to set the thread priorities, and then tell the world the new process state
  Apply（应用）这些计算的ADJ分数，包括：写入内核 sysfs 以更新Oom Adj分数；调用内核API来设置线程优先级，然后（kernel则更新）新的进程状态。

## Cycles, Cycles, Cycles

Another interesting aspect of the Oom Adjuster is the cycles of the dependencies.
A simple example would be like below illustration, process A is hosting a service which is bound by process B; meanwhile the process B is hosting a service which is bound by process A.
Oom Adjuster的另一个有趣的方面是依赖关系的循环。
一个简单的例子如下图所示，进程A托管一个由进程B绑定的服务；同时，进程B托管一个由进程a绑定的服务。
<pre>
  +-------------+           +-------------+
  |  Process A  | <-------- |  Process B  |
  | (service 1) | --------> | (service 2) |
  +-------------+           +-------------+
</pre>

There could be very complicated cases, which could involve multiple cycles, and in the dependency graph, each of the process record node could have different importance.
可能存在非常复杂的情况，可能涉及多个周期，并且在依赖关系图中，每个进程（记录节点）可能具有不同的重要性。
<pre>
  +-------------+           +-------------+           +-------------+           +-------------+           +-------------+
  |  Process D  | --------> |  Process A  | <-------- |  Process B  | <-------- |  Process C  | <-------- |  Process A  |
  |             |           | (service 1) |           | (service 2) |           | (service 3) |           | (service 1) |
  +-------------+           +-------------+           +-------------+           +-------------+           +-------------+
</pre>

The Oom Adjuster maintains a global sequence ID `mAdjSeq` to track the current Oom Adjuster calling.
And each of the process record has a field to track in which sequence the process record is evaluated.
If during the Oom Adj computation, a process record with sequence ID as same as the current global sequence ID, this would mean that a cycle is detected; in this case:
* Decrement the sequence ID of each process if there is a cycle.
* Re-evaluate each of the process record within the cycle until nothing was promoted.
* Iterate the processes from least important to most important ones.
* A maximum retries of 10 is enforced, while in practice, the maximum retries could reach only 2 to 3.
  Oom调节器维护一个全局ID`mAdjSeq`，用于追踪当前的Oom Adjuster调用。每个进程record都有一个字段，用于跟踪评估进程record的顺序。
  如果在Oom Adj计算期间，序列ID与当前全局序列ID相同的过程记录，这意味着检测到循环；在这种情况下：
  *如果有循环，则减少每个进程的序列ID。
  *重新评估周期内的每个过程记录，直到没有任何提升。
  *从最不重要的流程迭代到最重要的流程。
  *最大重试次数为10次，而实际上，最大重试次数只能达到2到3次。

