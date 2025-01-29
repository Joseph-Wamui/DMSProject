import { Component } from '@angular/core';
import  Chart  from 'chart.js/auto';

@Component({
  selector: 'app-piechart',
  templateUrl: './piechart.component.html',
  styleUrls: ['./piechart.component.css']
})
export class PiechartComponent {
  public chart: any;
  chartOptions: any;
 
  ngOnInit(): void {
     this.createChart();
  }
 
  createChart() {
     this.chart = new Chart("MyChart", {
       type: 'pie',
       data: {
         labels: ['Pending', 'Approved', 'Rejected', ],
         datasets: [{
           label: 'My First Dataset',
           data: [100, 120, 250, ],
           backgroundColor: [
             'grey',
             'green',
             'red',
             
           ],
           //hoverOffset: 4
         }],
       },
       options: {
         aspectRatio: 1.5
       }
     });
 
      this.chartOptions.js.chart('container', this.chartOptions);
    }
}
