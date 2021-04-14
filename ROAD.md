ROAD 
===

ROAD,  Router Operation Acceptable Dispatch, is a solution which uses [DESim4J](https://github.com/uia4j/uia-sim) to build a simulator of the manufacturing factory.

## Design Concept

The core elements are

* Factory - The factory.
* Op - The operation in the factory.
* Equip - The equipment in the factory.
    * Channel - The working unit in the equipment.
* Job - The job defines the routing sequence running in the factory.
* JobBox - The job collection.

The core helpers are

* Equipment
  * EquipStrategy - change move in/out timing.
  * JobSelector - select jobs from the operation.
  * ProcessTimeCalculator - calculate process time of a job.
* Factory
  * PathTimeCalculator - calculate moving time between two operations.

### Job
__Job__ is the most important element in the simulator. It is a two-way linked object that describes how to run a product step by step.

```
product1  : job1 <-> job2 <-> job3
```

Place any __ONE__ job of jobs sequence in the factory, the simulator will start processing the product form there.

```
factory.prepare(job2);
```


## Examples

Below is a simple example:

* 3 operations.
* 4 equipments.
* 2 jobs, all work from __o1__ to __o3__.

The router-operation map is

```txt
(j1,j2) o1-o2-o3
           e2
          /  \
        e1    e4
          \  /
           e3
```

Simulation

```java
Factory<Integer> factory = new Factory<>();
factory.setProcessTimeCalculator((e, j) -> j.getData().intValue());

// create operations
Op<Integer> o1 = factory.createOperation("o1");
Op<Integer> o2 = factory.createOperation("o2");
Op<Integer> o3 = factory.createOperation("o3");

// create equipments
Equip<Integer> e1 = factory.createEquip("e1", 2, 2);    // allow 2 jobs at the same time.
Equip<Integer> e2 = factory.createEquip("e2", 1, 1);
Equip<Integer> e3 = factory.createEquip("e3", 1, 1);
Equip<Integer> e4 = factory.createEquip("e4", 1, 1);    // allow 1 job only.

// bind operations and equipments
o1.serve(e1);
o2.serve(e2);
o2.serve(e3);
o3.serve(e4);

// prepare job1 (j1a -> j1b -> j1c)
Job<Integer> j1a = new Job<>("job1", o1.getId(), 100);
Job<Integer> j1b = new Job<>("job1", o2.getId(), 50);
Job<Integer> j1c = new Job<>("job1", o3.getId(), 150);
j1a.setNext(j1b).setNext(j1c);

// prepare job2 (j2a -> j2b -> j2c)
Job<Integer> j2a = new Job<>("job2", o1.getId(), 100);
Job<Integer> j2b = new Job<>("job2", o2.getId(), 50);
Job<Integer> j2c = new Job<>("job2", o3.getId(), 150);
j2a.setNext(j2b).setNext(j2c);

// place FIRST job of jobs sequence into the factory
factory.prepare(j1a);
factory.prepare(j2a);

// start to simulate 1000 time units
factory.run(1000);

// print simple logs
factory.getReport().printlnSimpleOpEvents();
factory.getReport().printlnSimpleEquipEvents();
factory.getReport().printlnSimpleJobEvents();

```

The simple output is
```txt
o1
  0:00:00        0 ENQUEUE        
  0:00:00        0 ENQUEUE        
  0:00:00        0 DEQUEUE        
  0:00:00        0 DEQUEUE        
o2
  0:01:40      100 ENQUEUE        
  0:01:40      100 ENQUEUE        
  0:01:40      100 DEQUEUE        
  0:01:40      100 DEQUEUE        
o3
  0:02:30      150 ENQUEUE        
  0:02:30      150 ENQUEUE        
  0:02:30      150 DEQUEUE        
  0:05:00      300 DEQUEUE        
e1
  0:00:00        0 MOVE_IN        
  0:00:00        0 MOVE_IN        
  0:00:00        0 BUSY           
  0:01:40      100 MOVE_OUT       
  0:01:40      100 MOVE_OUT       
e2
  0:01:40      100 MOVE_IN        
  0:01:40      100 BUSY           
  0:02:30      150 MOVE_OUT       
e3
  0:01:40      100 MOVE_IN        
  0:01:40      100 BUSY           
  0:02:30      150 MOVE_OUT       
e4
  0:02:30      150 MOVE_IN        
  0:02:30      150 BUSY           
  0:05:00      300 MOVE_OUT       
  0:05:00      300 MOVE_IN        
  0:05:00      300 BUSY           
  0:07:30      450 MOVE_OUT       
job2
  0:00:00        0 DISPATCHED     
  0:00:00        0 MOVE_IN        
  0:00:00        0 PROCESS_START  
  0:01:40      100 PROCESS_END    
  0:01:40      100 MOVE_OUT       
  0:01:40      100 DISPATCHING    
  0:01:40      100 DISPATCHED     
  0:01:40      100 MOVE_IN        
  0:01:40      100 PROCESS_START  
  0:02:30      150 PROCESS_END    
  0:02:30      150 MOVE_OUT       
  0:02:30      150 DISPATCHING    
  0:02:30      150 DISPATCHED     
  0:05:00      300 MOVE_IN        
  0:05:00      300 PROCESS_START  
  0:07:30      450 PROCESS_END    
  0:07:30      450 MOVE_OUT       
  0:07:30      450 DONE           
job1
  0:00:00        0 DISPATCHED     
  0:00:00        0 MOVE_IN        
  0:00:00        0 PROCESS_START  
  0:01:40      100 PROCESS_END    
  0:01:40      100 MOVE_OUT       
  0:01:40      100 DISPATCHING    
  0:01:40      100 DISPATCHED     
  0:01:40      100 MOVE_IN        
  0:01:40      100 PROCESS_START  
  0:02:30      150 PROCESS_END    
  0:02:30      150 MOVE_OUT       
  0:02:30      150 DISPATCHING    
  0:02:30      150 DISPATCHED     
  0:02:30      150 MOVE_IN        
  0:02:30      150 PROCESS_START  
  0:05:00      300 PROCESS_END    
  0:05:00      300 MOVE_OUT       
  0:05:00      300 DONE           
```
