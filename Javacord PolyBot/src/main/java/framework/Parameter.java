package framework;

import org.javacord.api.interaction.SlashCommandOptionType;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Parameters.class)
public @interface Parameter {
    String name();
    String description();
    boolean isRequired() default true;
    SlashCommandOptionType type() default SlashCommandOptionType.STRING;
}
