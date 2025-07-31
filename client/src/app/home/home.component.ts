import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})

export class HomeComponent {
  private router = inject(Router);

  navigateToRegister() {
    this.router.navigate(['/register']);
  }
}
