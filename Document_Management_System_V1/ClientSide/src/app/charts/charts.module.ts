import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PiechartComponent } from './piechart/piechart.component';
import { BarchartComponent } from './barchart/barchart.component';
import { RouterModule } from '@angular/router';
import { LineGraphComponent } from './linegraph/linegraph.component';
import { NgChartsModule } from 'ng2-charts';



@NgModule({
  declarations: [
    PiechartComponent,
    BarchartComponent,
    LineGraphComponent,
  
  ],
  imports: [
    CommonModule,
    RouterModule,
    NgChartsModule
  ],
  exports:[
    RouterModule,
    PiechartComponent,
    BarchartComponent,
    LineGraphComponent,
    NgChartsModule,
  ]
})
export class ChartsModule { }
