import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CategoryService } from '../../core/services/category.service';
import { Category, CategoryRequest } from '../../core/models/category.model';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.css']
})
export class CategoriesComponent implements OnInit {
  private readonly categoryService = inject(CategoryService);

  categories: Category[] = [];
  isLoading: boolean = false;
  isModalOpen: boolean = false;
  isEditing: boolean = false;
  editingId: number | null = null;
  form: CategoryRequest = { name: '', description: '', active: true };
  successMessage: string = '';
  errorMessage: string = '';

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.isLoading = true;
    this.categoryService.getAllCategories().subscribe({
      next: (data) => {
        this.categories = data;
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'No se pudieron cargar las categorías.';
        this.isLoading = false;
      }
    });
  }

  openCreateModal(): void {
    this.isEditing = false;
    this.editingId = null;
    this.form = { name: '', description: '', active: true };
    this.isModalOpen = true;
  }

  openEditModal(cat: Category): void {
    this.isEditing = true;
    this.editingId = cat.id;
    this.form = {
      name: cat.name,
      description: cat.description || '',
      active: cat.active
    };
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  saveCategory(): void {
    if (!this.form.name.trim()) {
      alert('El nombre de la categoría es obligatorio');
      return;
    }

    if (this.isEditing && this.editingId) {
      this.categoryService.updateCategory(this.editingId, this.form).subscribe({
        next: () => {
          this.showSuccess('Categoría actualizada exitosamente.');
          this.closeModal();
          this.loadCategories();
        },
        error: (err) => alert(err.error?.message || 'Error al actualizar categoría.')
      });
    } else {
      this.categoryService.createCategory(this.form).subscribe({
        next: () => {
          this.showSuccess('Categoría creada exitosamente.');
          this.closeModal();
          this.loadCategories();
        },
        error: (err) => alert(err.error?.message || 'Error al crear categoría.')
      });
    }
  }

  deleteCategory(cat: Category): void {
    if (confirm(`¿Desactivar la categoría "${cat.name}"?`)) {
      this.categoryService.deleteCategory(cat.id).subscribe({
        next: () => {
          this.showSuccess('Categoría desactivada.');
          this.loadCategories();
        },
        error: (err) => alert('Error al desactivar: ' + err.message)
      });
    }
  }

  private showSuccess(msg: string): void {
    this.successMessage = msg;
    setTimeout(() => this.successMessage = '', 4000);
  }
}
