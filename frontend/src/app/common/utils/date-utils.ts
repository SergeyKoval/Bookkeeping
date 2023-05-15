import { Injectable } from '@angular/core';

import { IMyDate } from 'angular-mydatepicker';

@Injectable()
export class DateUtils {
  public static MONTHS: string[] = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'];
  public static MONTH_LABELS: {} = { 1: 'Янв', 2: 'Фев', 3: 'Март', 4: 'Апр', 5: 'Май', 6: 'Июнь', 7: 'Июль', 8: 'Авг', 9: 'Сент', 10: 'Окт', 11: 'Ноя', 12: 'Дек' };
  public static DAY_LABELS: {} = {su: 'Вс', mo: 'Пн', tu: 'Вт', we: 'Ср', th: 'Чт', fr: 'Пт', sa: 'Сб'};
  private static DAYS_OF_WEEK: string[] = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

  public static convertDateToString(year: number, month: number, day: number): string {
    return `${day < 10 ? '0' : ''}${day}.${month < 10 ? '0' : ''}${month}.${year}`;
  }

  public static getDayOfWeek(date: Date): string {
    return DateUtils.DAYS_OF_WEEK[date.getDay()];
  }

  public static getUTCDate(date: Date = new Date(Date.now())): number {
    return DateUtils.initUTCDate(date.getFullYear(), date.getMonth(), date.getDate());
  }

  public static getUTCDateByDay(date: IMyDate): number {
    return DateUtils.initUTCDate(date.year, date.month - 1, date.day);
  }

  public static getDateFromUTC(timestamp: number = DateUtils.getUTCDate()): IMyDate {
    const date: Date = new Date(timestamp);
    return {year: date.getUTCFullYear(), month: date.getUTCMonth() + 1, day: date.getUTCDate()};
  }

  public static daysInMonth(year: number, month: number): number {
    return new Date(year, month, 0).getDate();
  }

  public static nextMonthPeriod(year: number, month: number): {year: number, month: number} {
    if (month < 12) {
      return {'year': year, 'month': month + 1};
    } else {
      return {'year': year + 1, 'month': 1};
    }
  }

  private static initUTCDate(year: number, month: number, day: number): number {
    return Date.UTC(year, month, day, 12, 0, 0, 0);
  }
}
