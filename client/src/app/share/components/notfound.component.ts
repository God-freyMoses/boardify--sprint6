import { Component, inject } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'not-found',
  template: `
    <div class="notfound-container">
      <div class="notfound-card">
        <h1 class="error-code">404</h1>
        <p class="error-message">Page Not Found</p>
        <button class="back-btn" (click)="goBack()">← Go Back</button>
      </div>
    </div>
  `,
  styles: [`
    :root {
      --primary-dark: #16423c;
      --primary-green: #6a9c89;
      --primary-light: #c4dad2;
      --background: #e9efec;
      --accent-orange: #e9762b;
      --white: #ffffff;
    }

    .notfound-container {
      height: 100vh;
      display: flex;
      justify-content: center;
      align-items: center;
      background: var(--background);
      font-family: 'Inter', sans-serif;
    }

    .notfound-card {
      background: var(--white);
      border-radius: 12px;
      padding: 40px 60px;
      text-align: center;
      box-shadow: 0px 4px 12px rgba(0,0,0,0.1);
    }

    .error-code {
      font-size: 96px;
      font-weight: bold;
      color: var(--primary-dark);
      margin-bottom: 10px;
    }

    .error-message {
      font-size: 20px;
      color: var(--primary-green);
      margin-bottom: 25px;
    }

    .back-btn {
      background: var(--primary-green);
      color: var(--white);
      padding: 10px 20px;
      border-radius: 6px;
      border: none;
      cursor: pointer;
      font-size: 16px;
      transition: background 0.3s ease;
    }

    .back-btn:hover {
      background: var(--primary-dark);
    }
  `]
})
export class NotFoundComponent {
  location: Location = inject(Location);

  goBack(): void {
    this.location.back();
  }
}
