import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.model';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // Comprobar roles requeridos si la ruta los especifica
  const expectedRoles = route.data?.['roles'] as Role[] | undefined;
  if (expectedRoles && expectedRoles.length > 0) {
    if (!authService.hasAnyRole(expectedRoles)) {
      alert('No tienes los permisos requeridos para acceder a este módulo.');
      router.navigate(['/inventory']);
      return false;
    }
  }

  return true;
};
