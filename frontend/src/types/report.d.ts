type SummaryReport = {
  category: string,
  values: {[currency: string]: number},
  percent?: number,
  subCategory?: string
};

type SummaryReportWrapper = {
  category: string,
  items: SummaryReport[]
};

type DynamicReport = {
  year: number,
  month: number,
  category: string,
  currency: string,
  value: number,
  subCategory?: string
};
