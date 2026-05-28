import { Component } from '@angular/core';
import { BeneficioPageComponent } from './modules/beneficios/components/beneficio-page/beneficio-page.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BeneficioPageComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent {}
