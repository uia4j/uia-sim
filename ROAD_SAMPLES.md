ROAD Examples
===

注意：泛型 \<T> 為實際應用的領域資訊 (Domain Information) ，以下範例固定為 \<Integer>，表示工作在站點所許的製造時間 (Process Time)。

## 第一次建立
1. 建立工廠

    ```java
    Factory<Integer> factory = new Factory<>();
    ```

2. 配置站點與設備

    配置兩個站點與三個設備，關係如圖：
    ```txt
    o1 - o2
         e2
       /
    e1
       \ 
         e3
    ```

    * 站點 o1 有 e1 設備支援，設備有兩個 load ports。
    * 站點 o2 有 e2 & e3 設備支援，每個設備有一個 load port。

    程式：

    ```java
    Op<Integer> o1 = factory.createOperation("o1");
    Op<Integer> o2 = factory.createOperation("o2");
    Equip<Integer> e1 = factory.createEquip("e1", 2, 2);
    Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
    Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
    o1.serve(e1);
    o2.serve(e2);
    o2.serve(e3);
    ```

3. 配置製造時間計算程式


    ```java
    factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());
    ```

4. 建立產品的工作流

    建立兩個產品的製造流程：

    * 產品 p1 依序經過站點 o1 & o2 ，製造時間分別為 100 & 50。 
    * 產品 p2 依序經過站點 o1 & o2 ，製造時間分別為 90 & 110。 


    程式：

    ```java
    // p1 (o1->02)
    Job<Integer> p1o1 = new Job<>("p1", o1.getId(), 100);
    Job<Integer> p1o2 = new Job<>("p1", o2.getId(), 50);
    p1o1.setNext(p1o2);

    // p2 (o1->02)
    Job<Integer> p2o1 = new Job<>("p2", o1.getId(), 90);
    Job<Integer> p2o2 = new Job<>("p2", o2.getId(), 110);
    p2o1.setNext(p2o2);
    ```

5. 將生產工作配置進工廠

    兩個產品皆由 o1 開始製造。

    ```java
    factory.prepare(p1o1);  // p1 at o1
    factory.prepare(p2o1);  // p2 at o1
    ```

6. 啟動模擬

    模擬 500 個時間單位。

    ```java
    factory.run(500);
    ```

7. 輸出生產紀錄

    ```java
    factory.getReport().printlnSimpleJobEvents();
    ```

完整程式碼：

```java
Factory<Integer> factory = new Factory<>();

Op<Integer> o1 = factory.createOperation("o1");
Op<Integer> o2 = factory.createOperation("o2");
Equip<Integer> e1 = factory.createEquip("e1", 2, 2);
Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
o1.serve(e1);
o2.serve(e2);
o2.serve(e3);

factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());

// p1
Job<Integer> p1o1 = new Job<>("p1", o1.getId(), 100);
Job<Integer> p1o2 = new Job<>("p1", o2.getId(), 50);
p1o1.setNext(p1o2);
p1o2.setPrev(p1o1);

// p2
Job<Integer> p2o1 = new Job<>("p2", o1.getId(), 90);
Job<Integer> p2o2 = new Job<>("p2", o2.getId(), 110);
p2o1.setNext(p2o2);
p2o2.setPrev(p2o1);

factory.prepare(p1o1);
factory.prepare(p2o1);

factory.run(500);

factory.getReport().printlnSimpleJob();
```

輸出結果：
```txt
p1
  0:00:00        0 DISPATCHED      - o1
  0:00:00        0 MOVE_IN         - e1
  0:00:00        0 PROCESS_START   - e1_ch1
  0:01:40      100 PROCESS_END     - e1_ch1
  0:01:40      100 MOVE_OUT        - e1
  0:01:40      100 DISPATCHING     - o2
  0:01:40      100 DISPATCHED      - o2
  0:01:40      100 MOVE_IN         - e3
  0:01:40      100 PROCESS_START   - e3_ch1
  0:02:30      150 PROCESS_END     - e3_ch1
  0:02:30      150 MOVE_OUT        - e3
  0:02:30      150 DONE            - 
p2
  0:00:00        0 DISPATCHED      - o1
  0:00:00        0 MOVE_IN         - e1
  0:00:00        0 PROCESS_START   - e1_ch2
  0:01:30       90 PROCESS_END     - e1_ch2
  0:01:30       90 MOVE_OUT        - e1
  0:01:30       90 DISPATCHING     - o2
  0:01:30       90 DISPATCHED      - o2
  0:01:30       90 MOVE_IN         - e2
  0:01:30       90 PROCESS_START   - e2_ch1
  0:03:20      200 PROCESS_END     - e2_ch1
  0:03:20      200 MOVE_OUT        - e2
  0:03:20      200 DONE            - 
```