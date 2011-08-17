/*
This file is part of leafdigital util.

util is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

util is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with util.  If not, see <http://www.gnu.org/licenses/>.

Copyright 2011 Samuel Marshall.
*/
package util.clone;

/** 
 * This interface basically does the same as Cloneable. I can't remember why
 * I did it, but I think there was a good reason...
 */
public interface DeepCloneable
{
  /**
   * @return Cloned object
   */
  public Object cloneObject();
}