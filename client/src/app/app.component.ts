import { Component, inject, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HomeComponent } from './home/home.component';
import {TaskPageComponent} from './taskpage/taskpage.component';
import {UserdocumentsComponent} from './userdocuments/userdocuments.component';
import {OpendocumentsComponent} from './opendocuments/opendocuments.component';

import {PlanComponent} from './plan/plan.component';
import {PaymentpageComponent} from './paymentpage/paymentpage.component';
import {ResourcesComponent} from './resourcespage/resources.component';

import { EventPageComponent } from './eventpage/eventpage.component';
import { AuthInitService } from './auth/service/auth-init.service';


@Component({
  selector: 'app-root',
  standalone: true,

  imports: [RouterOutlet, HomeComponent, TaskPageComponent , UserdocumentsComponent , OpendocumentsComponent , PlanComponent , PaymentpageComponent , ResourcesComponent,  EventPageComponent],

  template: `
    <router-outlet></router-outlet>
  `
})
export class AppComponent implements OnInit {
  private authInitService = inject(AuthInitService);

  ngOnInit() {
    this.authInitService.initializeAuth();
  }
}
