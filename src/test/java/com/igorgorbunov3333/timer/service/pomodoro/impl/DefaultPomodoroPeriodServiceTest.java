package com.igorgorbunov3333.timer.service.pomodoro.impl;

import com.igorgorbunov3333.timer.model.dto.PomodoroDto;
import com.igorgorbunov3333.timer.model.entity.Pomodoro;
import com.igorgorbunov3333.timer.repository.PomodoroRepository;
import com.igorgorbunov3333.timer.service.mapper.PomodoroMapper;
import com.igorgorbunov3333.timer.service.util.CurrentDayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPomodoroPeriodServiceTest {

    @Mock
    private PomodoroRepository pomodoroRepository;
    @Mock
    private PomodoroMapper pomodoroMapper;
    @Mock
    private CurrentDayService currentDayService;

    @InjectMocks
    private DefaultPomodoroPeriodService testee;

    @Test
    void getCurrentWeekPomodoros_WhenNoWeeklyPomodoros_ThenReturnEmptyMap() {
        LocalDate currentDay = LocalDate.of(2022, 2, 5);
        when(currentDayService.getCurrentDay()).thenReturn(currentDay);
        LocalDateTime start = LocalDate.of(2022, 1, 31).atStartOfDay();
        LocalDateTime end = currentDay.atTime(LocalTime.MAX);
        when(pomodoroRepository.findByStartTimeAfterAndEndTimeBefore(start, end)).thenReturn(List.of());

        Map<DayOfWeek, List<PomodoroDto>> actual = testee.getCurrentWeekPomodoros();

        assertThat(actual).isEmpty();
    }

    @Test
    void getCurrentWeekPomodoros_WhenStartOfWeek_ThenReturnResult() {
        LocalDate currentDay = LocalDate.of(2022, 1, 31);
        when(currentDayService.getCurrentDay()).thenReturn(currentDay);
        LocalDateTime start = currentDay.atStartOfDay();
        LocalDateTime end = currentDay.atTime(LocalTime.MAX);

        Pomodoro firstPomodoro = mock(Pomodoro.class);
        when(firstPomodoro.getStartTime()).thenReturn(start.plusHours(3));
        Pomodoro secondPomodoro = mock(Pomodoro.class);
        when(secondPomodoro.getStartTime()).thenReturn(start.plusHours(4));
        when(pomodoroRepository.findByStartTimeAfterAndEndTimeBefore(start, end))
                .thenReturn(List.of(firstPomodoro, secondPomodoro));
        PomodoroDto firstPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto secondPomodoroDto = mock(PomodoroDto.class);
        when(pomodoroMapper.mapToDto(List.of(firstPomodoro, secondPomodoro)))
                .thenReturn(List.of(firstPomodoroDto, secondPomodoroDto));

        Map<DayOfWeek, List<PomodoroDto>> actual = testee.getCurrentWeekPomodoros();

        assertThat(actual)
                .containsExactlyEntriesOf(Map.of(DayOfWeek.MONDAY, List.of(firstPomodoroDto, secondPomodoroDto)));
    }

    @Test
    void getCurrentWeekPomodoros_WhenMiddleOfWeek_ThenReturnResult() {
        LocalDate currentDay = LocalDate.of(2022, 2, 2);
        when(currentDayService.getCurrentDay()).thenReturn(currentDay);
        LocalDateTime start = LocalDate.of(2022, 1, 31).atStartOfDay();
        LocalDateTime end = currentDay.atTime(LocalTime.MAX);

        Pomodoro firstDayPomodoro = mock(Pomodoro.class);
        when(firstDayPomodoro.getStartTime()).thenReturn(start.plusHours(3));
        Pomodoro secondDayPomodoro = mock(Pomodoro.class);
        when(secondDayPomodoro.getStartTime()).thenReturn(start.plusDays(1).plusHours(4));
        Pomodoro thirdDayPomodoro = mock(Pomodoro.class);
        when(thirdDayPomodoro.getStartTime()).thenReturn(start.plusDays(2).plusHours(4));

        when(pomodoroRepository.findByStartTimeAfterAndEndTimeBefore(start, end))
                .thenReturn(List.of(firstDayPomodoro, secondDayPomodoro, thirdDayPomodoro));
        PomodoroDto firstPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto secondPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto thirdPomodoroDto = mock(PomodoroDto.class);
        when(pomodoroMapper.mapToDto(List.of(firstDayPomodoro)))
                .thenReturn(List.of(firstPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(secondDayPomodoro)))
                .thenReturn(List.of(secondPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(thirdDayPomodoro)))
                .thenReturn(List.of(thirdPomodoroDto));

        Map<DayOfWeek, List<PomodoroDto>> actual = testee.getCurrentWeekPomodoros();

        Map<DayOfWeek, List<PomodoroDto>> expected = Map.of(
                DayOfWeek.MONDAY, List.of(firstPomodoroDto),
                DayOfWeek.TUESDAY, List.of(secondPomodoroDto),
                DayOfWeek.WEDNESDAY, List.of(thirdPomodoroDto));
        assertThat(actual)
                .containsExactlyEntriesOf(new TreeMap<>(expected));
    }

    @Test
    void getCurrentWeekPomodoros_WhenEndOfWeek_ThenReturnResult() {
        LocalDate currentDay = LocalDate.of(2022, 2, 6);
        when(currentDayService.getCurrentDay()).thenReturn(currentDay);
        LocalDateTime start = LocalDate.of(2022, 1, 31).atStartOfDay();
        LocalDateTime end = currentDay.atTime(LocalTime.MAX);

        Pomodoro firstDayPomodoro = mock(Pomodoro.class);
        when(firstDayPomodoro.getStartTime()).thenReturn(start.plusHours(3));
        Pomodoro secondDayPomodoro = mock(Pomodoro.class);
        when(secondDayPomodoro.getStartTime()).thenReturn(start.plusDays(1).plusHours(4));
        Pomodoro thirdDayPomodoro = mock(Pomodoro.class);
        when(thirdDayPomodoro.getStartTime()).thenReturn(start.plusDays(2).plusHours(4));
        Pomodoro fourthDayPomodoro = mock(Pomodoro.class);
        when(fourthDayPomodoro.getStartTime()).thenReturn(start.plusDays(3).plusHours(4));
        Pomodoro fifthDayPomodoro = mock(Pomodoro.class);
        when(fifthDayPomodoro.getStartTime()).thenReturn(start.plusDays(4).plusHours(4));
        Pomodoro sixthDayPomodoro = mock(Pomodoro.class);
        when(sixthDayPomodoro.getStartTime()).thenReturn(start.plusDays(5).plusHours(4));
        Pomodoro seventhDayPomodoro = mock(Pomodoro.class);
        when(seventhDayPomodoro.getStartTime()).thenReturn(start.plusDays(6).plusHours(4));

        when(pomodoroRepository.findByStartTimeAfterAndEndTimeBefore(start, end))
                .thenReturn(List.of(
                        firstDayPomodoro,
                        secondDayPomodoro,
                        thirdDayPomodoro,
                        fourthDayPomodoro,
                        fifthDayPomodoro,
                        sixthDayPomodoro,
                        seventhDayPomodoro
                ));
        PomodoroDto firstPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto secondPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto thirdPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto fourthPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto fifthPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto sixthPomodoroDto = mock(PomodoroDto.class);
        PomodoroDto seventhPomodoroDto = mock(PomodoroDto.class);
        when(pomodoroMapper.mapToDto(List.of(firstDayPomodoro)))
                .thenReturn(List.of(firstPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(secondDayPomodoro)))
                .thenReturn(List.of(secondPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(thirdDayPomodoro)))
                .thenReturn(List.of(thirdPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(fourthDayPomodoro)))
                .thenReturn(List.of(fourthPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(fifthDayPomodoro)))
                .thenReturn(List.of(fifthPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(sixthDayPomodoro)))
                .thenReturn(List.of(sixthPomodoroDto));
        when(pomodoroMapper.mapToDto(List.of(seventhDayPomodoro)))
                .thenReturn(List.of(seventhPomodoroDto));

        Map<DayOfWeek, List<PomodoroDto>> actual = testee.getCurrentWeekPomodoros();

        Map<DayOfWeek, List<PomodoroDto>> expected = Map.of(
                DayOfWeek.MONDAY, List.of(firstPomodoroDto),
                DayOfWeek.TUESDAY, List.of(secondPomodoroDto),
                DayOfWeek.WEDNESDAY, List.of(thirdPomodoroDto),
                DayOfWeek.THURSDAY, List.of(fourthPomodoroDto),
                DayOfWeek.FRIDAY, List.of(fifthPomodoroDto),
                DayOfWeek.SATURDAY, List.of(sixthPomodoroDto),
                DayOfWeek.SUNDAY, List.of(seventhPomodoroDto));
        assertThat(actual)
                .containsExactlyEntriesOf(new TreeMap<>(expected));
    }

}