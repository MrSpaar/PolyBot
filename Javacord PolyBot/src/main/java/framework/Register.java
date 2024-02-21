package framework;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.SlashCommandOptionType;
import org.javacord.api.listener.GloballyAttachableListener;

import java.util.ArrayList;

public class Register {
    private static long[] servers;
    private static final ArrayList<Group> groups = new ArrayList<>();

    public static void add(Group group) {
        groups.add(group);
    }

    public static void setServers(long... ids) {
        servers = ids;
    }

    public static void build(DiscordApi api) {
        ArrayList<SlashCommandBuilder> global = new ArrayList<>();
        ArrayList<SlashCommandBuilder> local = new ArrayList<>();

        groups.forEach(group -> {
            ArrayList<SlashCommandBuilder> builders = buildGroup(api, group);
            if (!builders.isEmpty() && group.isGlobal()) global.addAll(builders);
            else if (!builders.isEmpty()) local.addAll(builders);
        });

        api.bulkOverwriteGlobalApplicationCommands(global);
        for (long id: servers)
            api.bulkOverwriteServerApplicationCommands(id, local);
    }

    private static ArrayList<SlashCommandBuilder> buildGroup(DiscordApi api, Group group) {
        ArrayList<SlashCommandBuilder> builders = new ArrayList<>();

        switch (group.getType()) {
            case COMMANDS -> builders.addAll(buildCommandGroup(api, group));
            case SUB_COMMANDS -> builders.add(buildSubCommandGroup(api, group));
            case EVENTS -> addListeners(api, group);
        }

        return builders;
    }

    private static SlashCommandBuilder buildCommand(GloballyAttachableListener listener) {
        Command command = listener.getClass().getAnnotation(Command.class);

        return SlashCommand.with(command.name(), command.description(), getOptions(listener.getClass()));
    }

    private static ArrayList<SlashCommandBuilder> buildCommandGroup(DiscordApi api, Group group) {
        ArrayList<SlashCommandBuilder> builders = new ArrayList<>();

        for (GloballyAttachableListener listener: group.getListeners()) {
            api.addListener(listener);
            builders.add(buildCommand(listener));
        }

        return builders;
    }

    private static SlashCommandOption buildSubCommand(GloballyAttachableListener listener) {
        Command command = listener.getClass().getAnnotation(Command.class);

        return SlashCommandOption.createWithOptions(
                SlashCommandOptionType.SUB_COMMAND, command.name(), command.description(), getOptions(listener.getClass())
        );
    }

    private static SlashCommandBuilder buildSubCommandGroup(DiscordApi api, Group group) {
        ArrayList<SlashCommandOption> options = new ArrayList<>();

        for (GloballyAttachableListener listener: group.getListeners()) {
            api.addListener(listener);
            options.add(buildSubCommand(listener));
        }

        return SlashCommand.with(group.getName(), "Base", options);
    }

    private static void addListeners(DiscordApi api, Group group) {
        for (GloballyAttachableListener listener: group.getListeners())
            api.addListener(listener);
    }

    private static ArrayList<SlashCommandOption> getOptions(Class<? extends GloballyAttachableListener> cls) {
        ArrayList<SlashCommandOption> options = new ArrayList<>();
        Parameter[] parameters = cls.getAnnotationsByType(Parameter.class);

        for (Parameter parameter: parameters)
            options.add(SlashCommandOption.create(parameter.type(), parameter.name(), parameter.description(), parameter.isRequired()));

        return options;
    }
}
