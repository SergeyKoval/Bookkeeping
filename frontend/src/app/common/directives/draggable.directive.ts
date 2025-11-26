import { AfterViewInit, Directive, ElementRef, Input, NgZone, OnDestroy } from '@angular/core';
import { fromEvent, Observable, Subject } from 'rxjs';
import { map, switchMap, takeUntil } from 'rxjs/operators';

@Directive({
    selector: '[bkDraggable]',
    standalone: false
})
export class DraggableDirective implements AfterViewInit, OnDestroy {
  @Input()
  public dragHandle: string;
  @Input()
  public dragTarget: string;

  // Element to be dragged
  private target: HTMLElement;
  // Drag handle
  private handle: HTMLElement;
  private delta: {x: number, y: number} = {x: 0, y: 0};
  private offset: {x: number, y: number} = {x: 0, y: 0};

  private DESTROY$$: Subject<void> = new Subject<void>();

  public constructor(private _elementRef: ElementRef, private _zone: NgZone) { }

  public ngAfterViewInit(): void {
    this.handle = this.dragHandle ? document.querySelector(this.dragHandle) as HTMLElement : this._elementRef.nativeElement;
    this.target = document.querySelector(this.dragTarget) as HTMLElement;
    this.setupEvents();
  }

  public ngOnDestroy(): void {
    this.DESTROY$$.next();
  }

  private setupEvents(): void {
    this._zone.runOutsideAngular(() => {
      const mouseMove$: Observable<Event> = fromEvent(document, 'mousemove');
      const mouseUp$: Observable<Event> = fromEvent(document, 'mouseup');

      fromEvent(this.handle, 'mousedown').pipe(
        switchMap((event: MouseEvent) => {
          const startX: number = event.clientX;
          const startY: number = event.clientY;

          return mouseMove$.pipe(
            map((mouseEvent: MouseEvent) => {
              mouseEvent.preventDefault();
              this.delta = {
                x: mouseEvent.clientX - startX,
                y: mouseEvent.clientY - startY
              };
            }),
            takeUntil(mouseUp$)
          );
        }),
        takeUntil(this.DESTROY$$)
      ).subscribe(() => {
        if (this.delta.x === 0 && this.delta.y === 0) {
          return;
        }

        this.translate();
      });

      mouseUp$.pipe(takeUntil(this.DESTROY$$)).subscribe(() => {
        this.offset.x += this.delta.x;
        this.offset.y += this.delta.y;
        this.delta = {x: 0, y: 0};
      });
    });
  }

  private translate(): void {
    requestAnimationFrame(() => {
      this.target.style.transform = `translate(${this.offset.x + this.delta.x}px,${this.offset.y + this.delta.y}px)`;
    });
  }
}
