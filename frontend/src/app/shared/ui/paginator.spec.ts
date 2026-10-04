import { TestBed } from '@angular/core/testing';
import { describe, expect, it } from 'vitest';
import { Page } from '../../core/models';
import { Paginator } from './paginator';

function render(page: Page<unknown>): HTMLElement {
  const fixture = TestBed.createComponent(Paginator);
  fixture.componentRef.setInput('page', page);
  fixture.detectChanges();
  return fixture.nativeElement as HTMLElement;
}

const pagina = (page: number, totalPages: number, size = 10): Page<unknown> => ({
  content: Array.from({ length: page + 1 < totalPages ? size : 3 }),
  page,
  size,
  totalElements: (totalPages - 1) * size + 3,
  totalPages,
});

describe('Paginator', () => {
  it('no se muestra sin resultados', () => {
    const el = render({ content: [], page: 0, size: 20, totalElements: 0, totalPages: 0 });
    expect(el.querySelector('nav')).toBeNull();
  });

  it('muestra el rango y el total', () => {
    const el = render(pagina(1, 3));
    expect(el.textContent).toContain('11–20');
    expect(el.textContent).toContain('23');
  });

  it('resume las páginas lejanas con …', () => {
    const el = render(pagina(5, 10));
    const botones = [...el.querySelectorAll('button')].map((b) => b.textContent!.trim());
    // anterior, 1, 5, 6, 7, 10, siguiente
    expect(botones.filter(Boolean)).toEqual(['1', '5', '6', '7', '10']);
    expect(el.querySelector('[aria-current="page"]')!.textContent!.trim()).toBe('6');
    expect(el.textContent!.match(/…/g)).toHaveLength(2);
  });

  it('emite la página pedida', () => {
    const fixture = TestBed.createComponent(Paginator);
    fixture.componentRef.setInput('page', pagina(0, 3));
    fixture.detectChanges();
    let pedida = -1;
    fixture.componentInstance.pagina.subscribe((n) => (pedida = n));
    (fixture.nativeElement as HTMLElement)
      .querySelector<HTMLButtonElement>('[aria-label="Página siguiente"]')!
      .click();
    expect(pedida).toBe(1);
  });
});
