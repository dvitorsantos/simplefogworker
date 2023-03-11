package lsdi.fogworker.Services;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import lombok.Data;
import lsdi.fogworker.DataTransferObjects.DeployRequest;
import lsdi.fogworker.Models.Event;
import org.springframework.stereotype.Service;

@Data
@Service
public class EsperService {
    private Configuration configuration;
    private CompilerArguments arguments;
    private EPCompiler compiler;
    private EPRuntime runtime;

    public EsperService() {
        configuration = new Configuration();
        configuration.getCommon().addEventType("Event", Event.class);
        arguments = new CompilerArguments(configuration);
        compiler = EPCompilerProvider.getCompiler();
        runtime = EPRuntimeProvider.getDefaultRuntime(configuration);
    }

    public EPCompiled compile(String epl) throws EPCompileException {
        return compiler.compile(epl, arguments);
    }

    public EPDeployment deploy(EPCompiled compiled) throws EPDeployException {
        return runtime.getDeploymentService().deploy(compiled);
    }

    public void undeploy(String deploymentId) throws EPUndeployException {
        runtime.getDeploymentService().undeploy(deploymentId);
    }

    public void sendEvent() {
        runtime.getEventService().sendEventBean(new Event("1.2", "3.4"), "Event");
    }

    public EPStatement getStatement(String deploymentId, String statementName) {
        return runtime.getDeploymentService().getStatement(deploymentId, statementName);
    }

    public static String buildEPL(DeployRequest deployRequest) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("@Name('");
        stringBuilder.append(deployRequest.getName());
        stringBuilder.append("')\n");
        stringBuilder.append(deployRequest.getRule());
        stringBuilder.append(";\n");
        return stringBuilder.toString();
    }
}
