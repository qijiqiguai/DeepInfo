package tech.qi.deepinfo.frame.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 任何第三方资源管理的, 都应该作为 Handler
 * todo 提供界面可配置管理方式
 */
public abstract class AbstractHandler implements Initable, Stoppable {

    private static Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

    private String name;

    public abstract void init();

    public abstract void reload();

    @Override
    public void stopMe(){
        logger.debug(this.name + " stopped");
    }

    public AbstractHandler(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 执行
     * @param command
     */
    public void execute(Command command){
        if(command==null){
            logger.warn("command is null");
            return;
        }
        switch (command){
            case  INIT:
                init();
                return;
            case RELOAD:
                reload();
                return;
            case STOP:
                stopMe();
                return;
            default:
                logger.warn("无法处理命令："+command);
        }
    }

    /**
     * 执行
     * @param command
     */
    public void execute(String command){
        Command cmd = Command.parseCommand(command);
        execute(cmd);
    }

    public enum Command {
        INIT,RELOAD,STOP;

        public static Command parseCommand(String command){
            if(command.toUpperCase().equals(INIT.name())){
                return INIT;
            }else if(command.toUpperCase().equals(RELOAD.name())){
                return RELOAD;
            }else if(command.toUpperCase().equals(STOP.name())){
                return STOP;
            }else{
                return null;
            }
        }
    }
}
