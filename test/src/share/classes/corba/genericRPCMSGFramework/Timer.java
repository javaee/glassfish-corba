/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2001-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package corba.genericRPCMSGFramework;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * @author Harold Carr
 */
public class Timer
{
    public LinkedList times;

    public Timer()
    {
	times = new LinkedList();
    }

    public void begin()
    {
	times.add(0, new BeginEnd(System.currentTimeMillis()));
    }

    public void end()
    {
	((BeginEnd)times.get(0)).end = System.currentTimeMillis();
    }

    public void display()
    {
	Iterator iterator = times.iterator();
	while (iterator.hasNext()) {
	    System.out.println(iterator.next());
	}
    }

    public long average()
    {
	if (times.size() == 0) {
	    return 0;
	}
	long sum = 0;
	Iterator iterator = times.iterator();
	while (iterator.hasNext()) {
	    BeginEnd beginEnd = (BeginEnd) iterator.next();
	    sum += (beginEnd.end - beginEnd.begin);
	}
	return sum / times.size();
    }
	    
    public class BeginEnd
    {
	public long begin;
	public long end;
	public BeginEnd(long begin)
	{
	    this.begin = begin;
	    this.end = -1;
	}
	public String toString()
	{
	    return new Long(end - begin).toString();
	}
    }
}

// End of file.
