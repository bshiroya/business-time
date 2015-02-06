package com.github.aruberto.businesstime.common;

import net.objectlab.kit.datecalc.common.DateCalculator;

/**
 * Common date calculation methods
 *
 * @param <E> the type of date object
 */
public class BusinessDateTimeCalculator<E> {

  /**
   * Use {@code calc} to move {@code startDate} at time {@code startTimeMillisOfDay} by
   * {@code millisToMove} millis.
   *
   * @param startDate starting date
   * @param startTimeMillisOfDay starting time as millis since midnight
   * @param millisToMove the amount of millis to move, may be negative or positive
   * @param dayStartMillisOfDay business day start time as millis since midnight
   * @param dayEndMillisOfDay business day end time as millis since midnight
   * @param calc {@link net.objectlab.kit.datecalc.common.DateCalculator} to use to adjust days
   * @return date and millis of day after moving by specified millis
   */
  public BusinessDateTimeCalculatorResult<E> moveByMillis(E startDate,
                                                          int startTimeMillisOfDay,
                                                          long millisToMove,
                                                          int dayStartMillisOfDay,
                                                          int dayEndMillisOfDay,
                                                          DateCalculator<E> calc) {
    int millisPerDay = dayEndMillisOfDay - dayStartMillisOfDay;
    boolean isWorkingDay = !calc.isNonWorkingDay(startDate);
    boolean isStartBeforeBusinessDay = startTimeMillisOfDay < dayStartMillisOfDay;
    boolean isStartAfterBusinessDay = startTimeMillisOfDay > dayEndMillisOfDay;

    long daysAdjustment = 0;
    long totalMillis = millisToMove;

    if (isWorkingDay) {
      if (totalMillis >= 0) {
        // Adjust days/millis to be in reference to current business day at business hours start
        if (isStartBeforeBusinessDay) {
          // If start time is before business hours, then current business date time
          // is actually previous day at business end time
          daysAdjustment--;
          totalMillis += millisPerDay;
        } else {
          // Add millis elapsed in current business day
          totalMillis += Math.min(millisPerDay, startTimeMillisOfDay - dayStartMillisOfDay);
        }
      } else {
        // Adjust days/millis to be in reference to current business day at business hours end
        if (isStartAfterBusinessDay) {
          // If start time is after business hours, then current business date time
          // is actually next day at business start time
          daysAdjustment++;
          totalMillis -= millisPerDay;
        } else {
          // Subtract millis remaining in day
          totalMillis -= Math.min(millisPerDay, dayEndMillisOfDay - startTimeMillisOfDay);
        }
      }
    }

    // Calculate the number of days and millisOfDay to move.
    // millisOfDay should end up being <= millisPerDay.
    int days = (int) ((totalMillis - 1) / millisPerDay + daysAdjustment);
    int millisOfDay = (int) ((totalMillis - 1) % millisPerDay + 1);

    // Use DateCalculator to calculate new business day
    calc.setStartDate(startDate);
    if (days != 0) {
      calc = calc.moveByBusinessDays(days);
    }
    E endDate = calc.getCurrentBusinessDate();

    // When millisOfDay is positive, time was added and reference time is business hour start
    // When millisOfDay is negative, time was subtracted and reference time is business hour end
    int endTimeMillisOfDay = millisOfDay > 0 ? dayStartMillisOfDay : dayEndMillisOfDay;
    endTimeMillisOfDay += millisOfDay;

    return new BusinessDateTimeCalculatorResult<E>(endDate, endTimeMillisOfDay);
  }
}
