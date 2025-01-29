import { NgModule, Optional, SkipSelf } from "@angular/core";
import { CommonModule } from "@angular/common";
import { MatDialogModule } from "@angular/material/dialog";
//import { IdleTimer } from "./service/idletime.service";

@NgModule({
  declarations: [],
  imports: [CommonModule ,MatDialogModule],
  providers: [

  ],
})
export class CoreModule {
  constructor(@Optional() @SkipSelf() parentModule: CoreModule) {
    throwIfAlreadyLoaded(parentModule, "CoreModule");
  }
}
function throwIfAlreadyLoaded(parentModule: CoreModule, arg1: string) {
}

