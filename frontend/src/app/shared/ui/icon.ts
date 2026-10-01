import { Component, computed, input } from '@angular/core';

/** Geometría de un ícono de línea 24×24 (trazos tomados de Lucide, licencia ISC). */
interface IconDef {
  paths?: string[];
  circles?: [cx: number, cy: number, r: number][];
  rects?: [x: number, y: number, w: number, h: number, rx: number][];
}

const ICONS = {
  dashboard: {
    rects: [
      [3, 3, 7, 9, 1],
      [14, 3, 7, 5, 1],
      [14, 12, 7, 9, 1],
      [3, 16, 7, 5, 1],
    ],
  },
  cart: {
    circles: [
      [8, 21, 1],
      [19, 21, 1],
    ],
    paths: ['M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12'],
  },
  wallet: {
    paths: [
      'M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1',
      'M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4',
    ],
  },
  receipt: {
    paths: [
      'M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z',
      'M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8',
      'M12 17.5v-11',
    ],
  },
  users: {
    circles: [[9, 7, 4]],
    paths: [
      'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2',
      'M22 21v-2a4 4 0 0 0-3-3.87',
      'M16 3.13a4 4 0 0 1 0 7.75',
    ],
  },
  scissors: {
    circles: [
      [6, 6, 3],
      [6, 18, 3],
    ],
    paths: ['M20 4 8.12 15.88', 'M14.47 14.48 20 20', 'M8.12 8.12 12 12'],
  },
  chart: { paths: ['M3 3v18h18', 'M18 17V9', 'M13 17V5', 'M8 17v-3'] },
  shield: {
    paths: [
      'M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z',
      'm9 12 2 2 4-4',
    ],
  },
  settings: {
    circles: [[12, 12, 3]],
    paths: [
      'M12.22 2h-.44a2 2 0 0 0-2 2v.18a2 2 0 0 1-1 1.73l-.43.25a2 2 0 0 1-2 0l-.15-.08a2 2 0 0 0-2.73.73l-.22.38a2 2 0 0 0 .73 2.73l.15.1a2 2 0 0 1 1 1.72v.51a2 2 0 0 1-1 1.74l-.15.09a2 2 0 0 0-.73 2.73l.22.38a2 2 0 0 0 2.73.73l.15-.08a2 2 0 0 1 2 0l.43.25a2 2 0 0 1 1 1.73V20a2 2 0 0 0 2 2h.44a2 2 0 0 0 2-2v-.18a2 2 0 0 1 1-1.73l.43-.25a2 2 0 0 1 2 0l.15.08a2 2 0 0 0 2.73-.73l.22-.39a2 2 0 0 0-.73-2.73l-.15-.08a2 2 0 0 1-1-1.74v-.5a2 2 0 0 1 1-1.74l.15-.09a2 2 0 0 0 .73-2.73l-.22-.38a2 2 0 0 0-2.73-.73l-.15.08a2 2 0 0 1-2 0l-.43-.25a2 2 0 0 1-1-1.73V4a2 2 0 0 0-2-2z',
    ],
  },
  percent: {
    circles: [
      [6.5, 6.5, 2.5],
      [17.5, 17.5, 2.5],
    ],
    paths: ['M19 5 5 19'],
  },
  panel: { rects: [[3, 3, 18, 18, 2]], paths: ['M9 3v18'] },
  building: {
    paths: [
      'M6 22V4a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v18Z',
      'M6 12H4a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2h2',
      'M18 9h2a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-2',
      'M10 6h4',
      'M10 10h4',
      'M10 14h4',
      'M10 18h4',
    ],
  },
  chevronLeft: { paths: ['m15 18-6-6 6-6'] },
  chevronRight: { paths: ['m9 18 6-6-6-6'] },
  key: {
    circles: [[7.5, 15.5, 5.5]],
    paths: ['m21 2-9.6 9.6', 'm15.5 7.5 3 3L22 7l-3-3'],
  },
  logout: { paths: ['M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4', 'm16 17 5-5-5-5', 'M21 12H9'] },
} satisfies Record<string, IconDef>;

export type IconName = keyof typeof ICONS;

/** `<sf-icon name="cart" class="size-5" />` — hereda el color del texto (currentColor). */
@Component({
  selector: 'sf-icon',
  host: { class: 'inline-flex shrink-0', 'aria-hidden': 'true' },
  template: `
    <svg
      class="size-full"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="1.75"
      stroke-linecap="round"
      stroke-linejoin="round"
    >
      @for (r of def().rects ?? []; track $index) {
        <rect
          [attr.x]="r[0]"
          [attr.y]="r[1]"
          [attr.width]="r[2]"
          [attr.height]="r[3]"
          [attr.rx]="r[4]"
        />
      }
      @for (c of def().circles ?? []; track $index) {
        <circle [attr.cx]="c[0]" [attr.cy]="c[1]" [attr.r]="c[2]" />
      }
      @for (d of def().paths ?? []; track $index) {
        <path [attr.d]="d" />
      }
    </svg>
  `,
})
export class Icon {
  readonly name = input.required<IconName>();
  protected readonly def = computed<IconDef>(() => ICONS[this.name()]);
}
