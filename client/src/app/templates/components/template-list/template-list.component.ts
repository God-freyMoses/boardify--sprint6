import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { TemplateService } from '../../services/template.service';
import { Template, TemplateStatus, TemplateStatusEnum } from '../../models/template.model';
import { Store } from '@ngrx/store';
import { selectUser } from '../../../auth/store/auth.selectors';
import { UserType } from '../../../auth/model/auth.model';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-template-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './template-list.component.html',
  styleUrls: ['./template-list.component.css']
})
export class TemplateListComponent implements OnInit {
  private templateService = inject(TemplateService);
  private router = inject(Router);
  private store = inject(Store);

  templates: Template[] = [];
  filteredTemplates: Template[] = [];
  searchTerm = '';
  selectedStatus: TemplateStatus | 'ALL' = 'ALL';
  loading = false;
  error = '';
  
  user$: Observable<UserType | null> = this.store.select(selectUser);
  currentUser: UserType | null = null;

  templateStatuses = Object.values(TemplateStatusEnum);

  ngOnInit() {
    this.user$.subscribe(user => {
      this.currentUser = user;
    });
    this.loadTemplates();
  }

  loadTemplates() {
    this.loading = true;
    this.error = '';
    
    this.templateService.getAllTemplates().subscribe({
      next: (templates) => {
        this.templates = templates;
        this.applyFilters();
        this.loading = false;
      },
      error: (error) => {
        this.error = 'Failed to load templates';
        this.loading = false;
        console.error('Error loading templates:', error);
      }
    });
  }

  applyFilters() {
    this.filteredTemplates = this.templates.filter(template => {
      const matchesSearch = template.title.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           template.description.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = this.selectedStatus === 'ALL' || template.status === this.selectedStatus;
      return matchesSearch && matchesStatus;
    });
  }

  onSearchChange() {
    this.applyFilters();
  }

  onStatusChange() {
    this.applyFilters();
  }

  createTemplate() {
    this.router.navigate(['/templates/create']);
  }

  editTemplate(id: number) {
    this.router.navigate(['/templates', id, 'edit']);
  }

  assignTemplate(id: number) {
    this.router.navigate(['/templates', id, 'assign']);
  }

  deleteTemplate(id: number, title: string) {
    if (confirm(`Are you sure you want to delete the template "${title}"?`)) {
      this.templateService.deleteTemplate(id).subscribe({
        next: () => {
          this.loadTemplates();
        },
        error: (error) => {
          this.error = 'Failed to delete template';
          console.error('Error deleting template:', error);
        }
      });
    }
  }

  getStatusClass(status: TemplateStatus): string {
    switch (status) {
      case 'ACTIVE':
        return 'status-active';
      case 'INACTIVE':
        return 'status-inactive';
      case 'DRAFT':
        return 'status-draft';
      default:
        return 'status-default';
    }
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString();
  }

  logout() {
    this.router.navigate(['/login']);
  }

  navigateToDashboard() {
    this.router.navigate(['/dashboard']);
  }

  navigateToNewHires() {
    this.router.navigate(['/dashboard/newhires']);
  }
}