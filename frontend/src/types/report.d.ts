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
