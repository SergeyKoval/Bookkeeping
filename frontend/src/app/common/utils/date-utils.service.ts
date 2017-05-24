import { Injectable } from '@angular/core';

@Injectable()
export class DateUtilsService {
  private static DAYS_OF_WEEK: string[] = ['Воскресенье', 'Понедельник', 'Вторник', 'Среда', 'Четверг', 'Пятница', 'Суббота'];

  public static convertDateToString(date: Date): string {
    const day: number = date.getDate();
    const month: number = date.getMonth() + 1;
    return `${day < 10 ? '0' : ''}${day}.${month < 10 ? '0' : ''}${month}.${date.getFullYear()}`;
  }

  public static getDayOfWeek(date: Date): string {
    return DateUtilsService.DAYS_OF_WEEK[date.getDay()];
  }
}
