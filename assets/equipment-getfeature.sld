<?xml version="1.0" encoding="ISO-8859-1"?>
<StyledLayerDescriptor version="1.0.0"
                       xsi:schemaLocation="http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd"
                       xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc"
                       xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    <NamedLayer>
        <Name>Equipement</Name>
        <UserStyle>
            <Title>Hypervoly equipment</Title>
            <FeatureTypeStyle>
                <Rule>
                    <Title>Equipement</Title>
                    <PointSymbolizer>
                        <Graphic>
                            <ExternalGraphic>
                                <!--
                                here we don't care about representation, we just need an object that has correct size
                                changing for ${famille} leads to a NPE
                                 -->
                                <OnlineResource xlink:type="simple" xlink:href="icons/EP_ARMOIRE.svg"/>
                                <Format>image/svg</Format>
                            </ExternalGraphic>
                            <Size>48</Size>
                        </Graphic>
                    </PointSymbolizer>
                </Rule>
            </FeatureTypeStyle>
        </UserStyle>
    </NamedLayer>
</StyledLayerDescriptor>
