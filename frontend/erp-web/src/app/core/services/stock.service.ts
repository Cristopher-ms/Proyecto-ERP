import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { StockAdjustmentRequest, StockMovement } from '../models/stock-movement.model';
import { PaginatedResponse } from '../models/paginated-response.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/inventory`;

  adjustStock(request: StockAdjustmentRequest): Observable<StockMovement> {
    return this.http.post<StockMovement>(`${this.apiUrl}/adjust`, request);
  }

  getMovements(page: number = 0, size: number = 15): Observable<PaginatedResponse<StockMovement>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PaginatedResponse<StockMovement>>(`${this.apiUrl}/movements`, { params });
  }

  getMovementsByProduct(productId: number): Observable<StockMovement[]> {
    return this.http.get<StockMovement[]>(`${this.apiUrl}/movements/product/${productId}`);
  }
}
