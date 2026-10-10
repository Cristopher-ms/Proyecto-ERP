import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { LoginRequest } from '../../core/models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  form: LoginRequest = {
    email: '',
    password: ''
  };

  isLoading: boolean = false;
  errorMessage: string = '';

  onSubmit(): void {
    if (!this.form.email || !this.form.password) {
      this.errorMessage = 'Por favor ingresa tu correo y contraseña.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.form).subscribe({
      next: () => {
        this.isLoading = false;
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/inventory';
        this.router.navigateByUrl(returnUrl).catch(() => {
          this.router.navigate(['/inventory']);
        });
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Error al iniciar sesión. Verifica tus credenciales.';
      }
    });
  }

  // Atajos rápidos para demostración a reclutadores
  fillCredentials(role: 'admin' | 'vendedor' | 'almacen'): void {
    switch (role) {
      case 'admin':
        this.form = { email: 'admin@erp.com', password: 'Admin123!' };
        break;
      case 'vendedor':
        this.form = { email: 'vendedor@erp.com', password: 'Vendedor123!' };
        break;
      case 'almacen':
        this.form = { email: 'almacen@erp.com', password: 'Almacen123!' };
        break;
    }
    this.errorMessage = '';
  }
}
