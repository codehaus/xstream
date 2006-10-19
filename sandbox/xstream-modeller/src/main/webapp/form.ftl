<form action="webModeller.translate.logic" id="myForm">
Enter your xml in the following box:<br/>
<textarea cols="100" rows="30" name="xml">${xml!"<book>
	<authors>
		<author>
			<name>Guilherme Silveira</name>
		</author>
		<author id=\"15\">
			<name>Second author</name>
		</author>
	</authors>
	<coauthors>
		<author>
			<name>Who knows?</name>
			<country>ru</country>
		</author>
		<author>
			<name>Whoever</name>
			<country>de</country>
		</author>
	</coauthors>
	<title>My book title</title>
	<price>12.5</price>
</book>
"}</textarea><br/>
<input type="submit" value="Translate"/><br/>
</form>
