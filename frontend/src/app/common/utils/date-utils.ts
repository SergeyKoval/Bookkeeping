import { Injectable } from '@angular/core';

import { IMyDate } from 'mydatepicker';

@Injectable()
export class DateUtils {
  private static DAYS_OF_WEEK: string[] = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

  public static convertDateToString(date: Date): string {
    const day: number = date.getDate();
    const month: number = date.getMonth() + 1;
    return `${day < 10 ? '0' : ''}${day}.${month < 10 ? '0' : ''}${month}.${date.getFullYear()}`;
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

  private static initUTCDate(year: number, month: number, day: number): number {
    return Date.UTC(year, month, day, 12, 0, 0, 0);
  }
}
