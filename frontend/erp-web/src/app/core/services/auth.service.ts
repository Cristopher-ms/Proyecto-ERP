import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap, catchError, of, throwError, timeout } from 'rxjs';
import { User, LoginRequest, RegisterRequest, AuthResponse, Role } from '../models/auth.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  // Endpoint de autenticación (Gateway / Microservicio Auth)
  private readonly authUrl = (environment as any).authUrl || 'http://localhost:8081/api/v1/auth';

  private readonly TOKEN_KEY = 'erp_access_token';
  private readonly REFRESH_KEY = 'erp_refresh_token';
  private readonly USER_KEY = 'erp_user_data';

  // Signals reactivos para el estado del usuario (Angular 18)
  readonly currentUser = signal<User | null>(this.loadStoredUser());
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly userRole = computed(() => this.currentUser()?.role || null);

  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, credentials).pipe(
      timeout(3500),
      tap((res) => this.setSession(res)),
      catchError((err) => {
        // Si el microservicio auth en Azure aún no está desplegado o está en arranque en frío
        if (err.name === 'TimeoutError' || err.status === 0 || err.status === 404 || err.status === 504) {
          console.warn('Backend de autenticación no alcanzable temporalmente. Activando sesión de demostración.');
          const mockRes = this.createMockSession(credentials.email);
          this.setSession(mockRes);
          return of(mockRes);
        }
        return throwError(() => err);
      })
    );
  }

  register(data: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.authUrl}/register`, data).pipe(
      tap((res) => this.setSession(res))
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_KEY);
  }

  hasRole(role: Role): boolean {
    const current = this.currentUser();
    return current !== null && current.role === role;
  }

  hasAnyRole(roles: Role[]): boolean {
    const current = this.currentUser();
    return current !== null && roles.includes(current.role);
  }

  private setSession(authResponse: AuthResponse): void {
    localStorage.setItem(this.TOKEN_KEY, authResponse.accessToken);
    localStorage.setItem(this.REFRESH_KEY, authResponse.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResponse.user));
    this.currentUser.set(authResponse.user);
  }

  private loadStoredUser(): User | null {
    const data = localStorage.getItem(this.USER_KEY);
    if (!data) return null;
    try {
      return JSON.parse(data) as User;
    } catch {
      return null;
    }
  }

  // Demo fallback para que el reclutador pueda explorar todos los roles de inmediato
  private createMockSession(email: string): AuthResponse {
    let role: Role = 'ROLE_ADMIN';
    let fullName = 'Administrador ERP';

    if (email.toLowerCase().includes('vendedor')) {
      role = 'ROLE_SELLER';
      fullName = 'Carlos Vendedor';
    } else if (email.toLowerCase().includes('almacen')) {
      role = 'ROLE_WAREHOUSE';
      fullName = 'Marcos Almacenero';
    }

    const mockUser: User = {
      id: 1,
      fullName,
      email,
      role,
      roleDisplayName: role === 'ROLE_ADMIN' ? 'Administrador' : role === 'ROLE_SELLER' ? 'Vendedor' : 'Almacenero',
      active: true,
      createdAt: new Date().toISOString()
    };

    return {
      accessToken: 'demo_jwt_token_for_portfolio_' + Date.now(),
      refreshToken: 'demo_refresh_token_' + Date.now(),
      tokenType: 'Bearer',
      expiresInMs: 86400000,
      user: mockUser
    };
  }
}
