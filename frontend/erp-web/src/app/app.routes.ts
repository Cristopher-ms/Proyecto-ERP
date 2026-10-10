import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login.component';
import { InventoryComponent } from './features/inventory/inventory.component';
import { CategoriesComponent } from './features/categories/categories.component';
import { MovementsComponent } from './features/movements/movements.component';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: 'login', component: LoginComponent, title: 'Iniciar Sesión - ERP Cloud' },
  {
    path: 'inventory',
    component: InventoryComponent,
    canActivate: [authGuard],
    title: 'Inventario - ERP Cloud'
  },
  {
    path: 'categories',
    component: CategoriesComponent,
    canActivate: [authGuard],
    title: 'Categorías - ERP Cloud'
  },
  {
    path: 'movements',
    component: MovementsComponent,
    canActivate: [authGuard],
    title: 'Movimientos - ERP Cloud'
  },
  { path: '', redirectTo: 'inventory', pathMatch: 'full' },
  { path: '**', redirectTo: 'inventory' }
];
