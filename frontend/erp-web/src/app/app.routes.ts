import { Routes } from '@angular/router';
import { InventoryComponent } from './features/inventory/inventory.component';
import { CategoriesComponent } from './features/categories/categories.component';
import { MovementsComponent } from './features/movements/movements.component';

export const routes: Routes = [
  { path: '', redirectTo: 'inventory', pathMatch: 'full' },
  { path: 'inventory', component: InventoryComponent, title: 'Inventario - ERP Cloud' },
  { path: 'categories', component: CategoriesComponent, title: 'Categorías - ERP Cloud' },
  { path: 'movements', component: MovementsComponent, title: 'Movimientos - ERP Cloud' },
  { path: '**', redirectTo: 'inventory' }
];
