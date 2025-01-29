import { Component } from '@angular/core';
import  Chart  from 'chart.js/auto';

@Component({
  selector: 'app-barchart',
  templateUrl: './barchart.component.html',
  styleUrls: ['./barchart.component.css']
})
export class BarchartComponent {
  public chart: any;
 chartOptions: any;
 
 ngOnInit(): void {
     this.createChart();
 }
 
 createChart() {
     this.chart = new Chart("bChart", {
       type: 'bar',
       data: {
         labels: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
         datasets: [
           {
             label: "Approved",
             data: [467, 576, 572, 179, 292, 474, 373, 576, 534, 551, 432, 591],
             backgroundColor: 'green'
           },
           {
             label: "Pending",
             data: [543, 532, 539, 327, 97, 163, 538, 541, 516, 522, 566, 587],
             backgroundColor: 'grey'
           },
           {
            label: "Rejected",
            data: [541, 545, 536, 327, 217, 145, 538, 571, 427, 327, 308, 363],
            backgroundColor: 'red'
          }
         ]
       },
       options: {
        aspectRatio: 1.5,
        scales: {
          x: {
            title: {
              display: true,
              text: 'Months'
            }
          },
          y: {
            title: {
              display: true,
              text: 'Data Volume'
            }
          }
        }
  }
});
}

}
