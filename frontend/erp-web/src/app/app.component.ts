import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'ERP Cloud Enterprise';
  readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  isLoginPage(): boolean {
    return this.router.url.includes('/login') || !this.authService.isAuthenticated();
  }

  getUserInitials(): string {
    const user = this.authService.currentUser();
    if (!user || !user.fullName) return 'U';
    const parts = user.fullName.trim().split(' ');
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return user.fullName.substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}
