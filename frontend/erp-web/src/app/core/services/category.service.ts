import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, catchError, of, timeout } from 'rxjs';
import { Category, CategoryRequest } from '../models/category.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CategoryService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/categories`;

  getActiveCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(this.apiUrl).pipe(
      timeout(4000),
      catchError(() => of(this.getMockCategories()))
    );
  }

  getAllCategories(): Observable<Category[]> {
    return this.http.get<Category[]>(`${this.apiUrl}/all`).pipe(
      timeout(4000),
      catchError(() => of(this.getMockCategories()))
    );
  }

  getCategoryById(id: number): Observable<Category> {
    return this.http.get<Category>(`${this.apiUrl}/${id}`).pipe(
      timeout(3500),
      catchError(() => of(this.getMockCategories().find(c => c.id === id) || this.getMockCategories()[0]))
    );
  }

  createCategory(request: CategoryRequest): Observable<Category> {
    return this.http.post<Category>(this.apiUrl, request);
  }

  updateCategory(id: number, request: CategoryRequest): Observable<Category> {
    return this.http.put<Category>(`${this.apiUrl}/${id}`, request);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  mapToResponse(category: any): Category {
    return category;
  }

  private getMockCategories(): Category[] {
    return [
      { id: 1, name: 'Computación', description: 'Equipos de cómputo y laptops', active: true },
      { id: 2, name: 'Mobiliario y Oficina', description: 'Mobiliario corporativo', active: true },
      { id: 3, name: 'Periféricos', description: 'Monitores y accesorios', active: true },
      { id: 4, name: 'Redes y Conectividad', description: 'Routers, switches y cableado', active: true }
    ];
  }
}
