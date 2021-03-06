package org.nidget.eclipse.djeclipse.decompilers.cfr;

import org.benf.cfr.reader.api.ClassFileSource;
import org.benf.cfr.reader.entities.ClassFile;
import org.benf.cfr.reader.entities.Method;
import org.benf.cfr.reader.state.ClassFileSourceImpl;
import org.benf.cfr.reader.state.DCCommonState;
import org.benf.cfr.reader.state.TypeUsageCollector;
import org.benf.cfr.reader.util.CannotLoadClassException;
import org.benf.cfr.reader.util.getopt.GetOptParser;
import org.benf.cfr.reader.util.getopt.Options;
import org.benf.cfr.reader.util.getopt.OptionsImpl;
import org.benf.cfr.reader.util.output.Dumper;
import org.benf.cfr.reader.util.output.ToStringDumper;
import org.eclipse.core.runtime.IPath;
import org.nidget.eclipse.djeclipse.decompilers.exceptions.DecompilerException;

public class CFRDecompiler {

	public static String decompile(IPath jarPath, IPath classPath) {
		String classPathStr = classPath.toString();
		GetOptParser getOptParser = new GetOptParser();

		try {
			Options options = (Options)getOptParser.parse(new String[] {classPathStr}, OptionsImpl.getFactory());
			ClassFileSource classFileSource = new ClassFileSourceImpl(options);
			DCCommonState dcCommonState = new DCCommonState(options, classFileSource);
			Dumper dumper = new ToStringDumper();
			
			String path = (String)options.getOption(OptionsImpl.FILENAME);
			ClassFile c = dcCommonState.getClassFileMaybePath(path);
			dcCommonState.configureWith(c);
			try {
				c = dcCommonState.getClassFile(c.getClassType());
			} catch (CannotLoadClassException e) {}
			c.loadInnerClasses(dcCommonState);
			c.analyseTop(dcCommonState);
			
			TypeUsageCollector collectingDumper = new TypeUsageCollector(c);
			c.collectTypeUsages(collectingDumper);
			
			String methname = (String)options.getOption(OptionsImpl.METHODNAME);
			if (methname == null) {
				c.dump(dumper);
			} else {
				try {
					for (Method method : c.getMethodByName(methname)) {
						method.dump(dumper, true);
					}
				} catch (NoSuchMethodException e) {
					throw new Exception("No such method '" + methname + "'.");
				}
			}
			return dumper.toString();
		} catch (Exception rte) {
			throw new DecompilerException(rte);
		}
	}

}
