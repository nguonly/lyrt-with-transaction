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

#### Example on Unbinding: Phantom State
TODO: will explain later
