package at.conapi.messagemanager.bootstrap;

import org.update4j.LaunchContext;
import org.update4j.service.DefaultLauncher;

public class AppLauncher extends DefaultLauncher {

    private boolean launchFailed;
    private Exception launchError;

    @Override
    public void run(LaunchContext context) {
        try {
            launchFailed=false;
            super.run(context);
        }
        catch (RuntimeException e){
            launchFailed=true;
            e.printStackTrace();
            launchError = e;
            throw e;
        }
    }

    public boolean isLaunchFailed(){
        return launchFailed;
    }

    public Exception getLaunchError(){
        return launchError;
    }
}
