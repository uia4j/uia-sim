Core Design Concept
===

## Core Concept
DESim4J 的設計參考 SimPy，核心為 Env 和 Event。

* Env 內的 PriorityQueue 對所有的 Event 進行排序管理。
* 啟動模擬後，Env 會依序取得 Event，並執行 Event 內的 callable(s)。

## Timeout Event
當建立 Timeout 事件時，Timeout 會自動將自己註冊到 Env 的 Event 佇列中，排定被觸發的時間為 `env.now() + delayTime`。

```java
Env env = new Env();
Timeout timeout = env.timeout(100); // 自動加入 Event 佇列, 觸發時間 now + 100。
```

欲讓 Timeout 與 Process 交互影響，需在 Process 內 `yield` 此 Timeout 事件。 

```java
public class MyJob extends Processable {

    public MyJob() {
        super("MyJob");
    }

    public void run() {
        yield(env.timeout(100)); // 自動加入 Event 佇列，並與 Process 交互影響。
    }
}
```

## Process Event
Process 內的 `resume()` 方法為此事件處理的核心：

* `yield` 一個 Event 給 Process 後，Process 會將自己的 `resume()` 掛載到此 Event 的 callable 清單中。
* 流程被阻塞 (blocking)，等待釋放。
* 該 Event 被 Env 調用，Env 透過 callable 執行 Process 的 `resume()`，此時阻塞 (blocking) 的流程被釋放繼續進行。

> event.callable -> process.resume()

以下面的流程為例：

```java
// process implementation
public class MyJob extends Processable {

    public MyJob() {
        super("MyJob");
    }

    @Override
    public void run() {
        yield(env().timeout(100));  // 步驟一
        yield(env().timeout(200));  // 步驟二
    }
}

// main program
Env env = new Env();
env.process(new MyJob());
env.run();
```

1. 「步驟一」 `yield` 一個 `timeout(100)`，Env 排定執行時間為 __100__ (0 + 100)，並傳送給 Process。
   
   1.1 Process 收到此 Event，把 `resume()` 掛載至該事件的 callable 清單中。
   
   1.2 阻塞 (blocking) 「步驟一」。

2. Env 處理到 Event 佇列中的 `Timeout`，呼叫掛載的 callable (於 1.1 掛載)。
   
3. Process 的 `resume()` 被調用。

   3.1 釋放「步驟一」。

   3.2 「步驟二」 `yield` 一個 `timeout(200)`，Env 排定執行時間為 __300__ (100 + 200)，並傳送給 Process。
   
   3.3  Process 收到新的 Event，把 `resume()` 掛載至該事件的 callable 清單中。
   
   3.4 阻塞 (blocking) 「步驟二」。

4. 重複 2/3 釋放「步驟二」，同時完成流程工作。

5. Process 無 Event 需要再處理，將自己排入 Env 的 Event 佇列中，等待完成 (succeed)。

上述流程為整個 Framework 最重要的部分，從簡單到複雜的模擬情境，都不脫離上述模式。

## Process Integration
下面的範例把前面範例的 `timeout(200)` 移到 SubStep 中，並與 MainStep 連動。關鍵在「步驟三」建立兩者間的關聯：

> subStep.callable -> mainStep.resume()

```java
// process implementation
public class SubStep extends Processable {

    public SubStep() {
        super("SubStep");
    }

    @Override
    public void run() {
        yield(env().timeout(200));  // 步驟四
    }
}

public class MainStep extends Processable {

    public Down(Step2 subStep) {
        super("MainStep");
    }

    @Override
    public void run() {

        yield(env().timeout(100));                          // 步驟一
        SubStep subStep = env().process(new SubStep());     // 步驟二
        yield(subStep);                                     // 步驟三
    }
}

// main program
Env env = new Env();
env.process(new MainStep());
env.run();
```

1. 「步驟一」建立一個 timeout(100) 事件並阻塞 (blocking)，Env 於 now = 100 時釋放。

2. 「步驟二」建立一個 SubStep，加入一個 `timeout(100)`，Env 排定執行時間為 __300__ (100 + 200)。

3. 「步驟三」`yield` SubStep 給 __MainStep__ 的 Process。
   
   3.1 __MainStep__ 的 Process 將 `resume()` 掛載至 SubStep 的 callable 清單中。
   
   3.2 阻塞 (blocking) 「步驟三」。

4. 「步驟四」於 now = 300 被釋放，並將 SubStep 排入 Event 佇列中，等待完成 (succeed)。

5. Env 取得 SubStep， 呼叫掛載的 callable (於 3.1 掛載)。

6. __MainStep__ 的 `resume()` 被調用。

   6.1 釋放「步驟三」。

   6.2 將 __MainStep__ 排入 Env 的 Event 佇列中，等待完成 (succeed)。
