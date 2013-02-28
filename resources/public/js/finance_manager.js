 $(function() {
   $("#budget_datepicker").focusin(function (){
       $(this).datepicker({ altField: "#"+$(this).attr('id'),altFormat: "yy-mm-dd"})
       $("#budget_datepicker").change(function (){
         var mydate = new Date($(this).val());
         var monthNames = ["January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"];

         $("#budget_month").val(monthNames[mydate.getMonth()]);

         $("#budget_year").val(mydate.getFullYear());
        });
      });

     $("#exp_datepk_edit").focusin(function () {
       $(this).datepicker({ altField: "#"+$(this).attr('id'),altFormat: "yy-mm-dd"})
       $("#exp_datepk").change(function () {
         var exp_date = new Date($(this).val());
         var monthNames = ["January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"];

         $("#exp_month").val(monthNames[exp_date.getMonth()]);

       });
    });

    $("input#exp_datepk").focusin(function () {
       $(this).datepicker({ altField: "#"+$(this).attr('id'),altFormat: "yy-mm-dd"})
       $("#exp_datepk").change(function () {
         var exp_date = new Date($(this).val());
         var monthNames = ["January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"];

         $("#exp_month").val(monthNames[exp_date.getMonth()]);

       });
    });


     $("#expenseform").submit(function (e) {
       var total = $("#bgt-total").val();
       var exp_amount = $("#exp_amt").val();
       var bgt_total = +total + +exp_amount;
       var bgt_amount = $("#bgt-amt").val();
      if (bgt_total > (parseInt($("#bgt-amt").val()))) {
        alert("Your Budget Exceeds...!!!");
      }
     });

     $("#reminder_date").focusin(function () {
       $(this).datepicker({ altField: "#"+$(this).attr('id'),altFormat: "yy-mm-dd"})
     });
  });

