import { Component, OnInit } from '@angular/core';
import  Chart from 'chart.js/auto';
import { DashboardService } from 'src/app/services/dashboard.service';

@Component({
 selector: 'app-linegraph',
 templateUrl: './linegraph.component.html',
 styleUrls:['./linegraph.component.css'],
})
export class LineGraphComponent implements OnInit {
 public chart: any;
 chartOptions: any;
 documentData: number[]=[];
 
 
 constructor(
  private dataService: DashboardService,
 ){}


 ngOnInit(): void {
  this.documentCount();
  this.createChart();
}
documentCount() {
  this.dataService.documentCount().subscribe({
    next: (res) => {
      console.log("Document Count Response:", res);
      this.documentData = res.entity;
      console.log("Data", this.documentData);

      // Process the document data for the chart
      let dates = Object.keys(this.documentData);
      let counts = Object.values(this.documentData);

      // Sort the dates and corresponding counts
      const sortedData = dates.map((date, index) => ({ date, count: counts[index] }))
                              .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
      dates = sortedData.map(data => data.date);
      counts = sortedData.map(data => data.count);

      // Update the chart with the new data
      this.updateChart(dates, counts);
    },
    error: (error) => {
      console.log("Error for document Count: ", error);
    },
    complete: () => {}
  });
}

createChart() {
  const ctx = document.getElementById('lineChart') as HTMLCanvasElement;
  
  this.chart = new Chart(ctx, {
    type: 'line',
    data: {
      labels: [], // Initially empty, will be updated dynamically
      datasets: [
        {
          label: "Document Counts",
          data: [],
          borderColor: 'green',
          fill: false,
          tension: 0.4,
          //backgroundColor: 'transparent'
        }
      ]
    },
    options: {
      maintainAspectRatio: false, // Disable the maintain aspect ratio
      scales: {
        x: {
          title: {
            display: true,
            text: 'Time'
          }
        },
        y: {
          title: {
            display: true,
            text: 'Documents uploaded'
          }
        }
      }
    }
  });
}


updateChart(dates: any[], counts: number[]) {
  this.chart.data.labels = dates;
  this.chart.data.datasets[0].data = counts;
  this.chart.update();
}




}
