package com.igorgorbunov3333.timer.service.console.command.line.session.impl;

import com.igorgorbunov3333.timer.service.console.command.line.provider.CommandProvider;
import com.igorgorbunov3333.timer.service.console.command.line.session.PomodoroTagInfo;
import com.igorgorbunov3333.timer.service.console.command.line.session.TagSessionProcessor;
import com.igorgorbunov3333.timer.service.console.printer.PrinterService;
import com.igorgorbunov3333.timer.service.tag.TagService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TagCreationSessionProcessor implements TagSessionProcessor {

    private final static int TAG_MAX_LENGTH = 30;

    private final TagService tagService;
    private final PrinterService printerService;
    private final CommandProvider commandProvider;

    @Override
    public void process(List<PomodoroTagInfo> tagsWithNumbers) {
        printerService.print("Please provide tag name");

        String tagNameAnswer = provideAndValidateTagName();
        if (tagNameAnswer == null) {
            return;
        }

        String savedTagName = tagService.save(tagNameAnswer);

        printerService.print(String.format("Tag with name %s successfully saved", savedTagName));
    }

    private String provideAndValidateTagName() {
        do {
            String tagNameAnswer = commandProvider.provideLine();

            if (tagNameAnswer.equalsIgnoreCase("e")) {
                printerService.print("Tag menu abandoned"); //TODO: message duplication
                return null;
            }

            if (tagNameAnswer.length() > TAG_MAX_LENGTH) {
                printerService.print(String.format("Tag name size must not be more then %d characters", TAG_MAX_LENGTH));
            } else if (StringUtils.isEmpty(tagNameAnswer)) {
                printerService.print("Tag must not be empty, please try again or press \"e\" to exit");
            } else {
                return tagNameAnswer;
            }
        } while (true);
    }

    @Override
    public String action() {
        return "1";
    }

}
