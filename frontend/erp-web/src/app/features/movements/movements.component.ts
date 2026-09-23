import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StockService } from '../../core/services/stock.service';
import { StockMovement } from '../../core/models/stock-movement.model';

@Component({
  selector: 'app-movements',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './movements.component.html',
  styleUrls: ['./movements.component.css']
})
export class MovementsComponent implements OnInit {
  private readonly stockService = inject(StockService);

  movements: StockMovement[] = [];
  isLoading: boolean = false;
  currentPage: number = 0;
  pageSize: number = 15;
  totalPages: number = 1;
  totalElements: number = 0;

  ngOnInit(): void {
    this.loadMovements();
  }

  loadMovements(): void {
    this.isLoading = true;
    this.stockService.getMovements(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.movements = response.content;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error al cargar movimientos de almacén:', err);
        this.isLoading = false;
      }
    });
  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.currentPage = page;
      this.loadMovements();
    }
  }
}
