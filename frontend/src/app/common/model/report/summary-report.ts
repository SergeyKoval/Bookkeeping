export interface SummaryReport {
  category: string;
  values: {[currency: string]: number};
  percent?: number;
  subCategory?: string;
}
