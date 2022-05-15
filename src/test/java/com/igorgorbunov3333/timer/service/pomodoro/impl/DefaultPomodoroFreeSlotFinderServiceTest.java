package com.igorgorbunov3333.timer.service.pomodoro.impl;

import com.igorgorbunov3333.timer.model.dto.PeriodDto;
import com.igorgorbunov3333.timer.model.dto.PomodoroDto;
import com.igorgorbunov3333.timer.service.exception.PomodoroException;
import com.igorgorbunov3333.timer.service.pomodoro.DailyPomodoroService;
import com.igorgorbunov3333.timer.service.util.CurrentTimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPomodoroFreeSlotFinderServiceTest {

    @InjectMocks
    private DefaultPomodoroFreeSlotFinderService testee;

    @Mock
    private DailyPomodoroService dailyPomodoroService;
    @Mock
    private CurrentTimeService currentTimeService;

    @Test
    void find_WhenNoPomodorosInDay_ThenReturnNearestTimeWithEndingOneMinuteAgo() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 22, 0, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        PeriodDto actual = testee.findFreeSlotInCurrentDay();
        assertThat(actual.getStart()).isEqualTo(currentTime.minusMinutes(1L).minusMinutes(20L));
        assertThat(actual.getEnd()).isEqualTo(currentTime.minusMinutes(1L));
    }

    @Test
    void find_WhenTodayOnlyOnePomodoroLessThanTwentyMinutesAgo_ThenReturnSlotWithEndingOneMinuteBeforeThisPomodoroStarted() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 22, 0, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        ZonedDateTime pomodoroStartTime = currentTime.minusMinutes(30L).atZone(ZoneOffset.UTC);
        ZonedDateTime pomodoroEndTime = currentTime.minusMinutes(10L).atZone(ZoneOffset.UTC);
        PomodoroDto pomodoro = mockPomodoro(pomodoroStartTime, pomodoroEndTime);
        when(dailyPomodoroService.getDailyPomodoros())
                .thenReturn(new ArrayList<>(List.of(pomodoro)));

        PeriodDto actual = testee.findFreeSlotInCurrentDay();

        assertThat(actual.getStart()).isEqualTo(pomodoroStartTime.toLocalDateTime().minusMinutes(1L).minusMinutes(20L));
        assertThat(actual.getEnd()).isEqualTo(pomodoroStartTime.toLocalDateTime().minusMinutes(1L));
    }

    @Test
    void find_WhenTodayMultiplePomodorosAnd20MinutesAgoWasPomodoroAndThereIsGapBetweenThemMoreThan20Minutes_ThenReturnNearestSlotToNowTimeBetweenThesePomodoros() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 22, 0, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        ZonedDateTime firstPomodoroStartTime = currentTime.minusMinutes(30L).atZone(ZoneOffset.UTC);
        ZonedDateTime firstPomodoroEndTime = firstPomodoroStartTime.plusMinutes(20L);
        PomodoroDto firstPomodoro = mockPomodoro(firstPomodoroStartTime, firstPomodoroEndTime);

        ZonedDateTime secondPomodoroStartTime = firstPomodoroStartTime.minusMinutes(50L);
        ZonedDateTime secondPomodoroEndTime = secondPomodoroStartTime.plusMinutes(20L);
        PomodoroDto secondPomodoro = mockPomodoro(secondPomodoroStartTime, secondPomodoroEndTime);

        ZonedDateTime thirdPomodoroStartTime = secondPomodoroStartTime.minusMinutes(25L);
        ZonedDateTime thirdPomodoroEndTime = thirdPomodoroStartTime.plusMinutes(20L);
        PomodoroDto thirdPomodoro = mockPomodoro(thirdPomodoroStartTime, thirdPomodoroEndTime);

        when(dailyPomodoroService.getDailyPomodoros())
                .thenReturn(new ArrayList<>(List.of(firstPomodoro, secondPomodoro, thirdPomodoro)));

        PeriodDto actual = testee.findFreeSlotInCurrentDay();

        assertThat(actual.getStart()).isEqualTo(firstPomodoroStartTime.toLocalDateTime().minusMinutes(1L).minusMinutes(20L));
        assertThat(actual.getEnd()).isEqualTo(firstPomodoroStartTime.toLocalDateTime().minusMinutes(1L));
    }

    @Test
    void find_WhenTodayMultiplePomodorosAnd20MinutesAgoWasPomodoroAndNoGapBetween_ThenRetuenSlotBeforeFirstPomodoro() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 22, 0, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        ZonedDateTime firstPomodoroStartTime = currentTime.minusMinutes(30L).atZone(ZoneOffset.UTC);
        ZonedDateTime firstPomodoroEndTime = firstPomodoroStartTime.plusMinutes(20L);
        PomodoroDto firstPomodoro = mockPomodoro(firstPomodoroStartTime, firstPomodoroEndTime);

        ZonedDateTime secondPomodoroStartTime = firstPomodoroStartTime.minusMinutes(25L);
        ZonedDateTime secondPomodoroEndTime = secondPomodoroStartTime.plusMinutes(20L);
        PomodoroDto secondPomodoro = mockPomodoro(secondPomodoroStartTime, secondPomodoroEndTime);

        ZonedDateTime thirdPomodoroStartTime = secondPomodoroStartTime.minusMinutes(25L);
        ZonedDateTime thirdPomodoroEndTime = thirdPomodoroStartTime.plusMinutes(20L);
        PomodoroDto thirdPomodoro = mockPomodoro(thirdPomodoroStartTime, thirdPomodoroEndTime);

        when(dailyPomodoroService.getDailyPomodoros())
                .thenReturn(new ArrayList<>(List.of(firstPomodoro, secondPomodoro, thirdPomodoro)));

        PeriodDto actual = testee.findFreeSlotInCurrentDay();

        assertThat(actual.getStart()).isEqualTo(thirdPomodoroStartTime.toLocalDateTime().minusMinutes(1L).minusMinutes(20L));
        assertThat(actual.getEnd()).isEqualTo(thirdPomodoroStartTime.toLocalDateTime().minusMinutes(1L));
    }

    @Test
    void find_WhenTodayNoPomodorosAndDayStartedLessThan20Minutes_ThenThrowException() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 0, 10, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        assertThatExceptionOfType(PomodoroException.class)
                .isThrownBy(() -> testee.findFreeSlotInCurrentDay());
    }

    @Test
    void find_WhenTodayMultiplePomodorosAndNoGapsBetweenAndPeriodBetweenDayStartsTimeAndFirstPomodoroStartLessThan20Minutes_ThenThrowException() {
        LocalDateTime currentTime = LocalDateTime.of(2022, 5, 10, 1, 10, 0);
        when(currentTimeService.getCurrentDateTime()).thenReturn(currentTime);

        ZonedDateTime firstPomodoroStartTime = currentTime.minusMinutes(30L).atZone(ZoneOffset.UTC);
        ZonedDateTime firstPomodoroEndTime = firstPomodoroStartTime.plusMinutes(20L);
        PomodoroDto firstPomodoro = mockPomodoro(firstPomodoroStartTime, firstPomodoroEndTime);

        ZonedDateTime secondPomodoroStartTime = firstPomodoroStartTime.minusMinutes(25L);
        ZonedDateTime secondPomodoroEndTime = secondPomodoroStartTime.plusMinutes(20L);
        PomodoroDto secondPomodoro = mockPomodoro(secondPomodoroStartTime, secondPomodoroEndTime);

        when(dailyPomodoroService.getDailyPomodoros())
                .thenReturn(new ArrayList<>(List.of(firstPomodoro, secondPomodoro)));

        assertThatExceptionOfType(PomodoroException.class)
                 .isThrownBy(() -> testee.findFreeSlotInCurrentDay());
    }

    private PomodoroDto mockPomodoro(ZonedDateTime start, ZonedDateTime end) {
        PomodoroDto pomodoro = mock(PomodoroDto.class);
        when(pomodoro.getStartTime()).thenReturn(start);
        when(pomodoro.getEndTime()).thenReturn(end);

        return pomodoro;
    }

}
