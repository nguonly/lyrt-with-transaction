# lyrt-with-transaction
LyRT Role Runtime with support of Transaction is an extended work from original LyRT. This works demonstrates two key features:
- Runtime architecture that supports Unanticipated Adaptation
- Consistency of adaptation by means of **Transaction**

### Runtime Archiecture for Unanticipated Adaptation
TODO: Will explain later

### Consistency with Transaction
Inconsistency of adapation arises from the composition or binding of roles to the core object from different thread asynchronously. This problem obviously affects the on-going execution resulting in unexpected system state. To prevent from this problem, a concept of **Transaction** from the object level is proposed to safeguard the changing of behaviors of core objects inside a transaction.

#### Declaration
``` java
try(Transaction tx = new Transaction()){
  ...
}
```

#### Example on New Binding
``` java
//Transfer will be a core object
public class Transfer {
  public String send(){
    return "data";
  }
}

//This is Role
public class Encryption {
  public String send(){
    return "<E>data<E>";
  }
}

//this is Role
public class Compression {
  public String send(){
    return "<C>data<C>";
  }
}
```
##### Binding new role while transaction is executing

``` java
public class Networking{
  public static void main(String... args{
    RegistryManager reg = RegistryManager().getInstance();
    Transfer transfer = reg.newPlayer(Transfer.class); //Create core object
    Compartment comp = reg.newCompartment(Compartment.class); //Create compartment
    comp.activate(); //activate compartment for role binding
    
    //Thread to cause binding happening during the transaction
    Runnable newBinding = () -> { 
      reg.bind(comp, transfer, Encryption);
    };
    
    //Another transaction happening after binding
    Runnable txAfterBinding = () -> {
      try(Transaction tx = new Transaction()){
        for(int i=0; i<100; i++){
          String data = reg.invokeRole(comp, transfer, "send", String.class);
          System.out.println(data); //Print encryption "<E>data<E>"
          Thread.sleep(20);
        }
      }
    });
    
    //Thread pool with scheduling
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);
    //happen after main thread
    pool.schedule(newBinding, 50, TimeUnit.MILLISECONDS); 
    //happen after newBinding thread
    pool.schedule(txAfterBinding, 100, TimeUnit.MILLISECONDS); 
    
    //Transaction in main thread firstly executes
    try(Transaction tx = new Transaction()){
      for(int i=0; i<100; i++){
        String data = reg.invokeRole(comp, transfer, "send", String.class);
        System.out.println(data); //Print original behavior "data"
        Thread.sleep(30);
      }
    }
    
    pool.shutdown(); //terminate thread pools
    
    while(!pool.isTerminated()){} //wait until all threads finished
  }
}
```

##### Phantom State: Unbind role while transaction is executing
While executing transaction, an `unbind` operation is called from different thread. In this case, the current bound roles that asking for removal will not be removed immediately but marked as phantom state with the timestamp when the unbinding occurs. This phantom state still allows not only the roles' method invoked but also prevents from another transaction attempting to invoke from these roles. 

In the example below, we have three threads running in parallel. The main thread is executing a transaction which attempts to invoke `Encryption` role. Note that `Encryption` role is bound to core object `transfer` before any transactions are executed. The main thread will produce `<E>data<E>`. During this transaction running, **unbinding** thread is executed to unbind `Encryption` from `transfer`. The **unbinding** operation detects that there is a main thread executing, then rather than completely removing `Encryption` from the runtime, it marks `Encryption` role as **phantom** associated with timestamp. After that, there is another transaction to call `send()` method of the `transfer` core executing in parallel with the main thread. In this case, the original behavior of core will be invoked even though `Encryption` role is still there but it has already marked as phantom which should not be considered for method dispatch.

``` java
public class Networking{
  public static void main(String... args{
    RegistryManager reg = RegistryManager().getInstance();
    Transfer transfer = reg.newPlayer(Transfer.class); //Create core object
    Compartment comp = reg.newCompartment(Compartment.class); //Create compartment
    comp.activate(); //activate compartment for role binding
    
    transfer.bind(comp, p, Encryption.class, null, null);
    
    //Thread to cause binding happening during the transaction
    Runnable unbinding = () -> { 
      reg.unbind(comp, transfer, Encryption.class);
    };
    
    //Another transaction happening after binding
    Runnable txAfterBinding = () -> {
      try(Transaction tx = new Transaction()){
        for(int i=0; i<100; i++){
          String data = reg.invokeRole(comp, transfer, "send", String.class);
          System.out.println(data); //Print original behavior "data"
          Thread.sleep(20);
        }
      }
    });
    
    //Thread pool with scheduling
    ScheduledExecutorService pool = Executors.newScheduledThreadPool(3);
    //happen after main thread
    pool.schedule(unbinding, 50, TimeUnit.MILLISECONDS); 
    //happen after newBinding thread
    pool.schedule(txAfterBinding, 100, TimeUnit.MILLISECONDS); 
    
    //Transaction in main thread firstly executes
    try(Transaction tx = new Transaction()){
      for(int i=0; i<100; i++){
        String data = reg.invokeRole(comp, transfer, "send", String.class);
        System.out.println(data); //Print encryption "<E>data<E>"
        Thread.sleep(30);
      }
    }
    
    pool.shutdown(); //terminate thread pools
    
    while(!pool.isTerminated()){} //wait until all threads finished
  }
}
```
