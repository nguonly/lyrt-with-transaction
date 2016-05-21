package net.runtime.role.tax;


import net.runtime.role.actor.Compartment;

/**
 * Created by nguonly on 9/9/15.
 */
public class Main {
    public static void main(String[] args){
        Person alice = new Person("Alice");
        Person bob = new Person("Bob");
        Person ely = new Person("Ely");
        Person tony = new Person("Tony");

        TaxDepartment taxDepartment = Compartment.initialize(TaxDepartment.class,
                new Class[]{String.class}, new Object[]{"Tax Department"});



        Company company = Compartment.initialize(Company.class,
                new Class[]{String.class}, new Object[]{"Company"});

        company.setRevenue(10000);

        company.activate();
        alice.bind(Manager.class);
        bob.bind(Employee.class);
        ely.bind(Employee.class);
        tony.bind(Accountant.class);

        alice.role(Manager.class).assignTask();

        System.out.println("Company Revenue before Staff payment = " + company.getRevenue());

        tony.role(Accountant.class).paySalary();

        System.out.println("Company Revenue after Staff payment = " + company.getRevenue());

        company.deActivate();



        taxDepartment.activate();

        Person ana = new Person("Ana");
        Person tim = new Person("Tim");
        //System.out.println(ana.hashCode());
        ana.bind(TaxEmployee.class);

        company.bind(TaxPayer.class);
        //DumpHelper.dumpRelation();
        company.invoke("pay");

        tim.bind(FreeLance.class).bind(TaxPayer.class);
        tim.invoke("earn", void.class, new Class[]{double.class}, new Object[]{500});
        tim.invoke("pay");


        System.out.println("Tony is accountant: saving = " + tony.getSaving());
        System.out.println("Alice is manager: saving = " + alice.getSaving());
        System.out.println("Bob is employee: saving = " + bob.getSaving());
        System.out.println("Ely is employee: saving = " + ely.getSaving());
        //System.out.println("Tony is accountant: saving = " + tony.getSaving());

        System.out.println("Company Revenue after Tax = " + company.getRevenue());
        System.out.println("Tim money after Tax = " + tim.invoke("getMoney", double.class));
        System.out.println("Tax Department Revenue = " + taxDepartment.getRevenue());

        taxDepartment.deActivate();
    }
}
