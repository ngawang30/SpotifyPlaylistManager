package music.spotifyplaylistmanager;

//Command Pattern:Invoker

import java.util.ArrayList;

public class SPMInvoker {
    ArrayList<SPMCommand> commands = new ArrayList();
    
    public void executeSPMCommand(SPMCommand command){
        commands.add(command);
        command.execute();
    }
    
    public void undoLastCommand(){
        try{
            SPMCommand command = commands.removeLast();
            command.undo();
        } catch (Exception e){}
    }
}
