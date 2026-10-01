import { Component, ElementRef, effect, input, OnDestroy, viewChild } from '@angular/core';
import { BarController, BarElement, CategoryScale, Chart, LinearScale, Tooltip } from 'chart.js';
import { VentaDia } from '../../core/models';

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip);

const fmt = new Intl.NumberFormat('es-BO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

/** Barras de total vendido por día (una sola serie: un tono, sin leyenda). */
@Component({
  selector: 'sf-ventas-chart',
  template: `<div class="relative h-64">
    <canvas #canvas role="img" aria-label="Total vendido por día"></canvas>
  </div>`,
})
export class VentasChart implements OnDestroy {
  readonly datos = input.required<VentaDia[]>();
  readonly simbolo = input('Bs');
  private readonly canvas = viewChild.required<ElementRef<HTMLCanvasElement>>('canvas');
  private chart?: Chart;

  constructor() {
    effect(() => this.render(this.datos(), this.simbolo()));
  }

  ngOnDestroy(): void {
    this.chart?.destroy();
  }

  private render(datos: VentaDia[], simbolo: string): void {
    const labels = datos.map((d) => d.fecha.slice(8, 10) + '/' + d.fecha.slice(5, 7));
    const values = datos.map((d) => d.total);
    if (this.chart) {
      this.chart.data.labels = labels;
      this.chart.data.datasets[0].data = values;
      this.chart.update();
      return;
    }
    this.chart = new Chart(this.canvas().nativeElement, {
      type: 'bar',
      data: {
        labels,
        datasets: [
          {
            data: values,
            backgroundColor: '#be185d',
            hoverBackgroundColor: '#9d174d',
            borderRadius: { topLeft: 4, topRight: 4 },
            borderSkipped: 'bottom',
            maxBarThickness: 28,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => {
                const d = datos[ctx.dataIndex];
                return ` ${simbolo} ${fmt.format(d.total)} · ${d.cantidad} ventas`;
              },
            },
          },
        },
        scales: {
          x: { grid: { display: false }, ticks: { color: '#64748b' } },
          y: {
            beginAtZero: true,
            border: { display: false },
            grid: { color: '#e2e8f0' },
            ticks: { color: '#64748b', callback: (v) => `${simbolo} ${v}` },
          },
        },
      },
    });
  }
}
