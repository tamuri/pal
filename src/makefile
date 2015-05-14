### VARIABLES ###

PACKAGES = \
	pal.alignment \
	pal.distance \
	pal.eval \
	pal.gui \
	pal.io \
	pal.math \
	pal.mep \
	pal.misc \
	pal.popgen \
	pal.statistics \
	pal.substmodel \
	pal.tree \
	pal.datatype \
	pal.util \
	pal.coalescent \
        pal.xml \
        

VERSION = pal-1.4
	
### TARGETS ###

.PHONY: classes native clean install installclean \
	doc docclean zip zipclean fullclean zipsrc distrib

classes:
	(cd pal; $(MAKE) classes)

native:
	(cd pal; $(MAKE) native)

clean:
	(cd pal; $(MAKE) clean)

doc:
	javadoc -d ../doc/api $(PACKAGES)
	
docclean:
	(rm -r -f ../doc/api/; mkdir ../doc/api)

zip:	classes
	zip -r pal pal -n .class -i '*.class' -i '*/' -x '*CVS/'
	
zipclean:
	rm -f pal.zip

fullclean: clean docclean installclean zipclean
	
			
zipsrc:
	zip -r -9 pal-src pal -i '*.java' -i '*akefile*' -i '*.html' -i '*/'
	
distrib:
	(cd ../..; zip -r -9 $(VERSION).zip $(VERSION) -x '*CVS*' -x '*.directory'; cd $(VERSION)/src);
	

